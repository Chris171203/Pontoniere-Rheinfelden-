#!/usr/bin/env node
// Execute the JavaScript parser against the actual exported bytes, not source-string assertions.
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { Script } from 'node:vm';

const root = fileURLToPath(new URL('../', import.meta.url));
let scripts = 0;
for (const language of ['de', 'gsw']) {
  const path = `${root}iOS/App/Resources/internal-attendance-${language}.js`;
  new Script(readFileSync(path, 'utf8'), { filename: path });
  scripts++;
}
const fixture = readFileSync(`${root}iOS/Tests/PFVRAppTests/Resources/internal-fixture.html`, 'utf8');
for (const [, script] of fixture.matchAll(/<script>([\s\S]*?)<\/script>/g)) {
  new Script(script, { filename: 'internal-fixture.html' });
  scripts++;
}
console.log(`V8 parsed ${scripts} actual internal renderer/fixture scripts. DOM behavior requires the WKWebView tests.`);
