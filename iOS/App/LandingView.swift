import SwiftUI
import PFVRCore

struct LandingView: View {
    @EnvironmentObject private var state: AppState
    @State private var code = ""
    @State private var rejected = false
    var body: some View {
        PageScroll {
            VStack(spacing: 10) {
                Image("PFVRLogo").resizable().scaledToFit().frame(width: 90, height: 90)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .accessibilityHidden(true)
                Text("PFVR Rheinfelden").font(.title2.bold())
                Text(state.ui("Willkommen")).foregroundStyle(PFVRTheme.water)
            }.frame(maxWidth: .infinity).padding(.vertical, 16)

            LanguagePicker()

            PFVRCard(state.ui("Neu beim PFVR?")) {
                Text(state.ui("Schnuppertraining ist auch vor der Mitgliedschaft möglich. Infos zu Einstieg, Mitgliedschaft und Formularen findest du auf pfvr.ch."))
                    .font(.subheadline).foregroundStyle(.secondary)
                Link(state.ui("Schnuppertraining & Mitglied werden"), destination: PublicLinks.join)
                    .buttonStyle(.borderedProminent)
                HStack {
                    Link("Instagram", destination: PublicLinks.instagram)
                    Spacer()
                    Link("Facebook", destination: PublicLinks.facebook)
                }.font(.subheadline)
            }
            PFVRCard(state.ui("App freischalten")) {
                Text(state.ui("Diese App kann interne Vereinsinformationen anzeigen. Gib den Freigabecode ein."))
                    .font(.subheadline)
                SecureField("XXXX-XXXX-XXXX-XXXX", text: $code)
                    .textInputAutocapitalization(.characters).autocorrectionDisabled()
                    .textFieldStyle(.roundedBorder)
                    .submitLabel(.go)
                    .onSubmit(unlock)
                    .accessibilityLabel(state.ui("Freigabecode"))
                    .accessibilityIdentifier("gate.code")
                if rejected {
                    Text(state.ui("Code stimmt nicht.")).foregroundStyle(.red).font(.caption)
                        .accessibilityIdentifier("gate.error")
                }
                Button(state.ui("Freischalten"), action: unlock)
                    .buttonStyle(.borderedProminent).frame(maxWidth: .infinity)
                    .accessibilityIdentifier("gate.unlock")
                Text(state.ui("Der Code wird nur zur lokalen Erstfreigabe geprüft. Persönliche PFVR-Links bleiben weiterhin ausschließlich auf diesem Gerät."))
                    .font(.caption).foregroundStyle(.secondary)
            }
        }
        .accessibilityIdentifier("screen.landing")
    }
    private func unlock() { rejected = !state.unlock(code: code); code = "" }
}

struct LanguagePicker: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        HStack {
            languageButton("Deutsch", .german)
            languageButton("Schwiizerdütsch", .swissGerman)
        }
        .padding(4)
        .background(Color(uiColor: .tertiarySystemFill), in: RoundedRectangle(cornerRadius: 12))
    }
    private func languageButton(_ label: String, _ language: AppLanguage) -> some View {
        Button { state.language = language } label: {
            Text(label).font(.subheadline.weight(.medium)).frame(maxWidth: .infinity, minHeight: 40)
                .background(state.language == language ? PFVRTheme.card : Color.clear, in: RoundedRectangle(cornerRadius: 9))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("settings.language.\(language.rawValue)")
        .accessibilityAddTraits(state.language == language ? .isSelected : [])
    }
}
