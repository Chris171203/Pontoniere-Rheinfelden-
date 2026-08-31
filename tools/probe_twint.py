import json
import urllib.parse
import urllib.request

BASE = 'https://www.pfvr.ch/wp-json/wp/v2/'
UA = {'User-Agent': 'PFVR-Rheinfelden-App-Dev/0.5'}

def get(path):
    req = urllib.request.Request(BASE + path, headers=UA)
    with urllib.request.urlopen(req, timeout=20) as r:
        return json.loads(r.read().decode('utf-8'))

hits = []
for term in ('twint', 'vereinsbeiz', 'beiz', 'zahlung', 'qr'):
    try:
        rows = get('media?' + urllib.parse.urlencode({'search': term, 'per_page': 100, '_fields': 'id,slug,source_url,title,caption,description'}))
    except Exception as e:
        print('media search failed', term, e)
        continue
    for row in rows:
        text = json.dumps(row, ensure_ascii=False).lower()
        if any(x in text for x in ('twint', 'vereinsbeiz', 'beiz', 'zahlung')):
            hits.append(row)

# Also scan the newest media because WordPress search depends on attachment metadata.
for page in range(1, 6):
    try:
        rows = get('media?' + urllib.parse.urlencode({'per_page': 100, 'page': page, 'orderby': 'date', 'order': 'desc', '_fields': 'id,slug,source_url,title,caption,description'}))
    except Exception:
        break
    for row in rows:
        text = json.dumps(row, ensure_ascii=False).lower()
        if any(x in text for x in ('twint', 'vereinsbeiz', 'vereins-beiz', 'beiz', 'zahlung')):
            hits.append(row)

unique = {}
for h in hits:
    unique[h.get('id')] = h
print('TWINT/PAYMENT_MEDIA_HITS=' + str(len(unique)))
for h in unique.values():
    print(json.dumps(h, ensure_ascii=False))
