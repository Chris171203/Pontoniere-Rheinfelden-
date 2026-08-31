from pathlib import Path

p=Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
s=p.read_text(encoding='utf-8')

if 'TWINT_QR_PDF' in s:
    print('TWINT QR integration already applied')
    raise SystemExit(0)

def repl(old,new,name):
    global s
    n=s.count(old)
    if n != 1:
        raise SystemExit(f'{name}: expected 1 match, got {n}')
    s=s.replace(old,new,1)

repl(
    '    private static final String CLUB_PAYMENT_NOTE = "Konsumation Vereinsbeiz";\n',
    '    private static final String CLUB_PAYMENT_NOTE = "Konsumation Vereinsbeiz";\n    private static final String TWINT_QR_PDF = "https://www.pfvr.ch/wp-content/uploads/Seiten/vereinsbeiz_zahlung/Twint_QR.pdf";\n',
    'twint url'
)

old='''        LinearLayout tw=card(); tw.setOrientation(LinearLayout.VERTICAL); b.addView(tw,margin(-1,-2,0,0,0,12)); tw.addView(txt("TWINT",16,TEXT,true)); TextView ti=txt("Alternative. Ohne separaten Vereins-Zahlungslink öffnet die App deine TWINT-App; bezahlt wird über den bestehenden Vereins-QR.",13,MUTED,false); ti.setPadding(0,dp(4),0,dp(10)); tw.addView(ti); Button twb=btn("TWINT öffnen",Color.rgb(232,240,244),NAVY); twb.setOnClickListener(v->openPreferred(true,amount)); tw.addView(twb,new LinearLayout.LayoutParams(-1,dp(46)));'''
new='''        LinearLayout tw=card(); tw.setOrientation(LinearLayout.VERTICAL); b.addView(tw,margin(-1,-2,0,0,0,12)); tw.addView(txt("TWINT",16,TEXT,true)); TextView ti=txt("Alternative zur Bankzahlung. Der offizielle Vereinsbeiz-TWINT-QR liegt auf pfvr.ch und kann als PDF geöffnet bzw. gespeichert werden.",13,MUTED,false); ti.setPadding(0,dp(4),0,dp(10)); tw.addView(ti);
        Button twq=btn("Vereins-TWINT-QR öffnen",NAVY,Color.WHITE); twq.setOnClickListener(v->external(TWINT_QR_PDF)); tw.addView(twq,new LinearLayout.LayoutParams(-1,dp(48)));
        Button twb=btn("TWINT-App öffnen",Color.rgb(232,240,244),NAVY); twb.setOnClickListener(v->openPreferred(true,amount)); LinearLayout.LayoutParams twbp=new LinearLayout.LayoutParams(-1,dp(44)); twbp.setMargins(0,dp(8),0,0); tw.addView(twb,twbp);'''
repl(old,new,'twint card')

p.write_text(s,encoding='utf-8')
print('Applied official Vereinsbeiz TWINT QR link')
