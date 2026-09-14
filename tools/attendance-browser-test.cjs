// Exercises real DOM events and delayed website updates, using synthetic people only.
const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const out=process.argv[2];
const fixture=`<!doctype html><html><head><style>
button{background:#dddddd;color:#222222}button.active{background:#16863a;color:#ffffff}
</style></head><body><form><table><tr><th>Person</th><th>14.09.2026<br>Training</th></tr>
<tr><td>Test Alex</td><td><button type="button" id="food" name="choice" value="food" class="active">Ich komme, mit Essen</button><button type="button" id="nofood" name="choice" value="nofood">Ich komme, ohne Essen</button><button type="button" id="absent" name="choice" value="absent">Ich komme nicht</button></td></tr>
<tr><td>Test Bea</td><td><input type="button" id="inline" value="Ich komme, mit Essen" style="background:#16863a;color:white"></td></tr></table></form>
<script>window.calls=0;window.nodes={};['food','nofood','absent','inline'].forEach(id=>nodes[id]=document.getElementById(id));
document.querySelector('form').addEventListener('click',event=>{if(!event.target.matches('button'))return;calls++;setTimeout(()=>{document.querySelectorAll('button').forEach(b=>b.classList.remove('active'));event.target.classList.add('active')},650)});
nodes.inline.onclick=()=>{calls++;setTimeout(()=>{nodes.inline.style.backgroundColor='#c83737';nodes.inline.value='Ich komme nicht'},700)};
</script></body></html>`;

(async()=>{
 const browser=await chromium.launch({headless:true});
 try {
  for(const lang of ['de','gsw'])for(const theme of ['light','dark']){
   const page=await browser.newPage({viewport:{width:390,height:844}});
   const errors=[];page.on('pageerror',e=>errors.push(e.message));
   await page.route('https://fixture.invalid/**',route=>route.fulfill({contentType:'text/html',body:fixture}));
   await page.goto('https://fixture.invalid/');
   await page.evaluate(fs.readFileSync(path.join(out,`${lang}-${theme}.js`),'utf8'));
   assert.equal(await page.locator('.pfvr-attendance-mobile').count(),1);
   const background=id=>page.locator('#'+id).evaluate(el=>getComputedStyle(el).backgroundColor);
   assert.equal(await background('food'),'rgb(22, 134, 58)');
   assert.equal(await background('nofood'),'rgb(221, 221, 221)');
   await page.locator('#nofood').click();
   await page.waitForFunction(()=>getComputedStyle(document.getElementById('nofood')).backgroundColor==='rgb(22, 134, 58)');
   assert.equal(await background('food'),'rgb(221, 221, 221)');
   await page.locator('#absent').click();
   await page.waitForFunction(()=>getComputedStyle(document.getElementById('absent')).backgroundColor==='rgb(22, 134, 58)');
   assert.equal(await background('nofood'),'rgb(221, 221, 221)');
   await page.locator('#inline').click();
   await page.waitForFunction(()=>getComputedStyle(document.getElementById('inline')).backgroundColor==='rgb(200, 55, 55)');
   // A late text update must clear the old pseudo-label without overwriting the real value.
   await page.evaluate(()=>nodes.food.textContent='Ich komme nicht');
   await page.waitForFunction(()=>!document.getElementById('food').hasAttribute('data-pfvr-display-label'));
   assert.equal(await page.evaluate(()=>Object.entries(nodes).every(([id,node])=>node===document.getElementById(id))),true);
   assert.equal(await page.evaluate(()=>calls),3);
   assert.equal(await page.locator('#nofood').getAttribute('value'),'nofood');
   assert.deepEqual(errors,[]);
   await page.screenshot({path:path.join(out,`${lang}-${theme}.png`),fullPage:true});
   console.log(`PASS ${lang}/${theme}: class and inline colours, delayed responses, original events/values, stale label`);
   await page.close();
  }
 } finally {await browser.close()}
})().catch(error=>{console.error(error);process.exitCode=1});
