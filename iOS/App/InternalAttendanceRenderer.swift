import Foundation

enum InternalAttendanceRenderer {
    static let peopleKey = "pfvr-attendance-people-v4"
    static let readyScript = "!!document.querySelector('.pfvr-attendance-mobile .pfvr-attendance-matrix')"
    static let peopleScript = "!!(window.pfvrOpenPeopleManager && window.pfvrOpenPeopleManager())"

    enum RendererError: Error { case missingResource }

    /// JSON encoding is required here: the private link and local state are data, never script.
    static func literal(_ value: String) -> String {
        let data = try! JSONEncoder().encode(value)
        return String(decoding: data, as: UTF8.self)
            .replacingOccurrences(of: "\u{2028}", with: "\\u2028")
            .replacingOccurrences(of: "\u{2029}", with: "\\u2029")
    }

    static func script(language: String, dark: Bool, initialURL: URL, bundle: Bundle = .main) throws -> String {
        let name = "internal-attendance-" + (language == "gsw" ? "gsw" : "de")
        guard let resource = bundle.url(forResource: name, withExtension: "js") else { throw RendererError.missingResource }
        var script = try String(contentsOf: resource, encoding: .utf8)
        let tokens = ["__BG__": dark ? "#11171C" : "#F4F7F9", "__CARD__": dark ? "#1A2228" : "#FFFFFF",
                      "__SOFT__": dark ? "#232E36" : "#EDF3F6", "__TEXT__": dark ? "#ECF1F4" : "#15232E",
                      "__MUTED__": dark ? "#A0B0BA" : "#60717E", "__BORDER__": dark ? "#344550" : "#DCE5EA",
                      "__LINK__": dark ? "#5BBED5" : "#247E99", "__SCHEME__": dark ? "dark" : "light"]
        for (token, value) in tokens { script = script.replacingOccurrences(of: token, with: value) }
        return script.replacingOccurrences(of: "'__BASE_INTERNAL_URL__'", with: literal(initialURL.absoluteString))
    }

    /// Seed only participant preferences. Cookies, page contents and attendance submissions are ephemeral.
    static func bootstrap(peopleState: String?) -> String {
        let seed = peopleState.map { "try { if(!sessionStorage.getItem('pfvr-native-seeded')) { localStorage.setItem(\(literal(peopleKey)), \(literal($0))); } sessionStorage.setItem('pfvr-native-seeded','1'); } catch (_) {}" } ?? ""
        return """
        (function(){
          if(location.protocol !== 'https:' || location.hostname !== 'intern.pfvr.ch' || window.top !== window) return;
          \(seed)
          var originalSet=Storage.prototype.setItem, originalRemove=Storage.prototype.removeItem, originalClear=Storage.prototype.clear;
          var send=function(value){try{window.webkit.messageHandlers.pfvrPeople.postMessage({value:value});}catch(_){}};
          Storage.prototype.setItem=function(key,value){originalSet.call(this,key,value);if(this===localStorage&&key===\(literal(peopleKey)))send(String(value));};
          Storage.prototype.removeItem=function(key){originalRemove.call(this,key);if(this===localStorage&&key===\(literal(peopleKey)))send(null);};
          Storage.prototype.clear=function(){originalClear.call(this);if(this===localStorage)send(null);};
          // The personal URL must not be sent in the HTTP Referer to other origins.
          var referrer=document.createElement('meta');referrer.name='referrer';referrer.content='no-referrer';
          var enforceReferrer=function(){
            var parent=document.head||document.documentElement;
            if(parent&&referrer.parentNode!==parent)parent.appendChild(referrer);
            document.querySelectorAll('meta[name="referrer"]').forEach(function(meta){if(meta.content!=='no-referrer')meta.content='no-referrer';});
          };
          enforceReferrer();
          new MutationObserver(enforceReferrer).observe(document,{childList:true,subtree:true,attributes:true,attributeFilter:['content']});
        })();
        """
    }
}
