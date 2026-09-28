import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java', import.meta.url), 'utf8');
function extract(marker) {
  const start = source.indexOf(marker);
  assert.notEqual(start, -1, marker);
  const open = source.indexOf('{', start);
  let depth = 0;
  for (let index = open; index < source.length; index++) {
    if (source[index] === '{') depth++;
    else if (source[index] === '}' && --depth === 0) return source.slice(start, index + 1) + ';';
  }
  throw Error('unclosed function: ' + marker);
}

const old = {version: 4, identityScope: 'person-A', primary: 'Member A', desired: ['Member A', 'Additional A'], hidden: [], restoreValues: {'additional a': 'option-A'}, pendingAdd: null, rowNames: ['Member A']};
const entries = new Map([['pfvr-attendance-people-v4', JSON.stringify(old)]]);
const context = {
  identityScope: 'person-B', PEOPLE_KEY: 'pfvr-attendance-people-v4', LEGACY_PEOPLE_KEY: 'pfvr-attendance-people-v3',
  localStorage: {getItem: key => entries.get(key), setItem: (key, value) => entries.set(key, value)},
  dedupePeople: names => [...new Set(names)], isPlaceholderPersonName: () => false, cleanPersonName: value => value,
  samePersonName: (a, b) => a === b, listHasPerson: (names, value) => names.includes(value),
  isHiddenPerson: () => false,
  addDesiredPerson: (state, value) => {if (!state.desired.includes(value)) state.desired.push(value)},
  Date, JSON, Array
};
vm.createContext(context);
for (const marker of ['var savePeopleState=function', 'var adoptCurrentPeople=function', 'var readPeopleState=function', 'var shouldTakeSourceList=function', 'var loadPeopleState=function']) {
  vm.runInContext(extract(marker), context);
}
const state = vm.runInContext('loadPeopleState', context)(['Member B']);
assert.equal(state.identityScope, 'person-B');
assert.deepEqual([...state.desired], ['Member B']);
assert.deepEqual({...state.restoreValues}, {});
assert.equal(vm.runInContext('readPeopleState', context)().primary, 'Member B');
context.identityScope = 'person-A';
assert.equal(vm.runInContext('readPeopleState', context)(), null);
console.log('Intern person state is isolated across link identities.');
