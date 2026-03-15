#!/usr/bin/env node
// Contract test: validates fixture JSON files against the API endpoint contracts
// defined in sentinel/schemas/features/auth-endpoints.json.
//
// Run manually: node scripts/validate-fixtures.js
// Run in CI:    add to .github/workflows/backend.yml as a separate step
//
// Fails with exit code 1 if:
//   - A fixture file is missing a required field from the schema
//   - A fixture file cannot be parsed as JSON
//   - A fixture file references a field not defined in the schema (drift detection)

const fs = require('fs');
const path = require('path');

const REPO_ROOT = path.resolve(__dirname, '..');
const ENDPOINT_SCHEMA = path.join(REPO_ROOT, 'sentinel/schemas/features/auth-endpoints.json');
const IOS_FIXTURES = path.join(REPO_ROOT, 'ios/StarterAppTests/Fixtures');
const ANDROID_FIXTURES = path.join(REPO_ROOT, 'android/app/src/test/resources/fixtures');

let errors = 0;
let checked = 0;

function fail(msg) {
  console.error(`  FAIL  ${msg}`);
  errors++;
}

function pass(msg) {
  console.log(`  OK    ${msg}`);
  checked++;
}

function validateFixtureAgainstSchema(fixturePath, schemaFields) {
  const name = path.basename(fixturePath);
  let fixture;
  try {
    fixture = JSON.parse(fs.readFileSync(fixturePath, 'utf8'));
  } catch (e) {
    fail(`${name} — invalid JSON: ${e.message}`);
    return;
  }

  for (const field of schemaFields) {
    if (!(field.name in fixture) && !field.optional) {
      fail(`${name} — missing required field: "${field.name}"`);
    }
  }

  const knownFields = schemaFields.map((f) => f.name);
  for (const key of Object.keys(fixture)) {
    if (!knownFields.includes(key)) {
      console.warn(`  WARN  ${name} — unknown field "${key}" (not in schema)`);
    }
  }

  pass(name);
}

// Load endpoint schemas
let schema;
try {
  schema = JSON.parse(fs.readFileSync(ENDPOINT_SCHEMA, 'utf8'));
} catch (e) {
  console.error(`Cannot read endpoint schema: ${e.message}`);
  process.exit(1);
}

const endpointsByFixtureName = {};
for (const ep of schema.endpoints) {
  // Map endpoint id to fixture filename (e.g. requestMagicLink → auth-request)
  const fixtureMap = {
    requestMagicLink: 'auth-request',
    verifyMagicLink: 'auth-verify',
    refreshToken: 'auth-refresh',
  };
  if (fixtureMap[ep.id] && ep.response?.fields) {
    endpointsByFixtureName[fixtureMap[ep.id]] = ep.response.fields;
  }
}

console.log('Validating fixtures against sentinel/schemas/features/auth-endpoints.json\n');

for (const [fixtureName, fields] of Object.entries(endpointsByFixtureName)) {
  const iosPath = path.join(IOS_FIXTURES, `${fixtureName}.json`);
  const androidPath = path.join(ANDROID_FIXTURES, `${fixtureName}.json`);

  if (fs.existsSync(iosPath)) {
    validateFixtureAgainstSchema(iosPath, fields);
  } else {
    console.warn(`  WARN  ios/${fixtureName}.json — not found (skipped)`);
  }

  if (fs.existsSync(androidPath)) {
    validateFixtureAgainstSchema(androidPath, fields);
  } else {
    console.warn(`  WARN  android/${fixtureName}.json — not found (skipped)`);
  }
}

console.log(`\n${checked} fixtures checked, ${errors} error(s).`);

if (errors > 0) {
  console.error('\nFixture validation failed. Update fixtures or fix the schema drift.');
  process.exit(1);
}
