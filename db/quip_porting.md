# 🎭 High Low Jack - Quip Porting Guide

**Porting Personality Quips from Local PostgreSQL to Railway**

**Author:** Primus & Dale  
**Version:** 1.0.0  
**Date:** April 9, 2026  
**Status:** Production Ready

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Method 1: Dump & Restore (Recommended)](#method-1-dump--restore-recommended)
4. [Method 2: CSV Export/Import (Fastest)](#method-2-csv-exportimport-fastest)
5. [Method 3: Manual SQL (Small Datasets)](#method-3-manual-sql-small-datasets)
6. [Method 4: Railway CLI (Easiest)](#method-4-railway-cli-easiest)
7. [Verification](#verification)
8. [Troubleshooting](#troubleshooting)
9. [Keeping Quips in Sync](#keeping-quips-in-sync)
10. [Best Practices](#best-practices)

---

## Overview

The `personality_quips` table contains the witty commentary and personality responses that make High Low Jack come alive. This guide covers multiple methods for porting this data from your local PostgreSQL database to Railway's cloud PostgreSQL instance.

### What You'll Need

- ✅ Local PostgreSQL with `personality_quips` table populated
- ✅ Railway account with PostgreSQL service running
- ✅ Railway connection credentials
- ✅ `psql` command-line tool installed
- ✅ (Optional) Railway CLI for easier management

---

## Quick Start

**TL;DR - Fastest Path:**

```bash
# 1. Export from local
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  > personality_quips_data.sql

# 2. Get Railway connection string from dashboard
# Format: postgresql://postgres:PASSWORD@host:5432/railway

# 3. Import to Railway
psql "postgresql://postgres:PASSWORD@host:5432/railway" \
  -f personality_quips_data.sql

# 4. Verify
psql "postgresql://postgres:PASSWORD@host:5432/railway" \
  -c "SELECT COUNT(*) FROM personality_quips;"
```

---

## Method 1: Dump & Restore (Recommended)

**Best for:** Most situations, clean and reliable

### Step 1: Export from Local PostgreSQL

```bash
# Export JUST the personality_quips table data
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  --no-owner \
  --no-privileges \
  > personality_quips_data.sql
```

**What these flags mean:**
- `--table=personality_quips` - Export only this table
- `--data-only` - Skip CREATE TABLE, just INSERT statements
- `--column-inserts` - Use `INSERT INTO table (col1, col2) VALUES` format (more readable)
- `--no-owner` - Don't include ownership commands
- `--no-privileges` - Don't include GRANT/REVOKE commands

### Step 2: Get Railway Connection String

1. Go to Railway dashboard: https://railway.app
2. Select your High Low Jack project
3. Click on PostgreSQL service
4. Click "Connect" tab
5. Copy the **PostgreSQL Connection URL**

Example format:
```
postgresql://postgres:PASSWORD@containers-us-west-123.railway.app:5432/railway
```

### Step 3: Import to Railway

```bash
# Method A: Direct command line
psql "postgresql://postgres:PASSWORD@containers-us-west-123.railway.app:5432/railway" \
  -f personality_quips_data.sql

# Method B: Using environment variable
export DATABASE_URL="postgresql://postgres:PASSWORD@host:5432/railway"
psql $DATABASE_URL -f personality_quips_data.sql
```

### Step 4: Verify Import

```bash
# Check row count
psql "$DATABASE_URL" -c "SELECT COUNT(*) FROM personality_quips;"

# Check sample data
psql "$DATABASE_URL" -c "SELECT player_name, trigger_event, COUNT(*) 
FROM personality_quips 
GROUP BY player_name, trigger_event 
ORDER BY player_name;"
```

---

## Method 2: CSV Export/Import (Fastest)

**Best for:** Large datasets, faster transfer

### Step 1: Export to CSV

```bash
# On local machine
psql -U hlj_user -d highlowjack -c \
  "COPY personality_quips TO STDOUT WITH CSV HEADER" \
  > personality_quips.csv
```

### Step 2: Import CSV to Railway

```bash
# Connect to Railway
psql "postgresql://postgres:PASSWORD@railway-host:5432/railway"
```

Then in the `psql` session:

```sql
\COPY personality_quips FROM '/absolute/path/to/personality_quips.csv' WITH CSV HEADER;
```

**Note:** Use absolute path, not relative path!

### Step 3: Verify

```sql
-- In psql session
SELECT COUNT(*) FROM personality_quips;

-- Check data looks correct
SELECT * FROM personality_quips LIMIT 5;
```

---

## Method 3: Manual SQL (Small Datasets)

**Best for:** Few quips, troubleshooting, or learning

### Step 1: Generate INSERT Statements

```bash
psql -U hlj_user -d highlowjack << 'EOF'
SELECT 
  'INSERT INTO personality_quips (player_name, trigger_event, quip_text, weight) VALUES (' ||
  '''' || player_name || ''', ' ||
  '''' || trigger_event || ''', ' ||
  '''' || replace(quip_text, '''', '''''') || ''', ' ||
  weight || ');'
FROM personality_quips
ORDER BY player_name, trigger_event;
EOF
```

This outputs clean INSERT statements like:

```sql
INSERT INTO personality_quips (player_name, trigger_event, quip_text, weight) VALUES ('Preezbob', 'CUT_TWO', 'Not again...', 10);
INSERT INTO personality_quips (player_name, trigger_event, quip_text, weight) VALUES ('Kreep', 'WIN_MATCH', 'Victory is mine!', 8);
```

### Step 2: Copy Output

Copy all the INSERT statements from the terminal output.

### Step 3: Connect to Railway and Paste

```bash
# Connect to Railway
psql "postgresql://postgres:PASSWORD@railway-host:5432/railway"
```

Paste the INSERT statements directly into the psql session and press Enter.

---

## Method 4: Railway CLI (Easiest)

**Best for:** Regular updates, development workflow

### Step 1: Install Railway CLI

```bash
# Using npm
npm install -g @railway/cli

# OR using Homebrew (macOS/Linux)
brew install railway

# OR download from: https://docs.railway.app/develop/cli
```

### Step 2: Login to Railway

```bash
railway login
```

This opens your browser for authentication.

### Step 3: Link Your Project

```bash
cd /path/to/highlowjack
railway link
```

Select your High Low Jack project from the list.

### Step 4: Export Local Data

```bash
# Create the export file
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  > quips_for_railway.sql
```

### Step 5: Connect to Railway DB

```bash
# This opens psql connected directly to Railway!
railway connect postgres
```

### Step 6: Load Your Data

```sql
-- In the Railway psql session:
\i /absolute/path/to/quips_for_railway.sql

-- Verify
SELECT COUNT(*) FROM personality_quips;
```

---

## Verification

### Check Row Counts Match

```bash
# Local count
psql -U hlj_user -d highlowjack -c "SELECT COUNT(*) FROM personality_quips;"

# Railway count (use your connection method)
railway connect postgres -c "SELECT COUNT(*) FROM personality_quips;"
# OR
psql "$DATABASE_URL" -c "SELECT COUNT(*) FROM personality_quips;"
```

### Check Data Integrity

```sql
-- Connect to Railway
railway connect postgres

-- Check player distribution
SELECT player_name, COUNT(*) as quip_count
FROM personality_quips
GROUP BY player_name
ORDER BY player_name;

-- Check trigger distribution
SELECT trigger_event, COUNT(*) as quip_count
FROM personality_quips
GROUP BY trigger_event
ORDER BY trigger_event;

-- Spot check some actual quips
SELECT player_name, trigger_event, quip_text
FROM personality_quips
WHERE player_name = 'Preezbob'
LIMIT 5;
```

### Sample Data Comparison

```bash
# Create hash of local data
psql -U hlj_user -d highlowjack -c \
  "SELECT md5(string_agg(quip_text, '' ORDER BY id)) FROM personality_quips;" \
  > local_hash.txt

# Create hash of Railway data
railway connect postgres -c \
  "SELECT md5(string_agg(quip_text, '' ORDER BY id)) FROM personality_quips;" \
  > railway_hash.txt

# Compare
diff local_hash.txt railway_hash.txt
# Should be identical!
```

---

## Troubleshooting

### Error: "Table doesn't exist"

**Problem:** `personality_quips` table not created in Railway DB

**Solution:**

```bash
# Check if table exists
railway connect postgres -c "\dt personality_quips"

# If missing, apply schema first
railway connect postgres < /path/to/schema.sql

# Then import data
railway connect postgres < personality_quips_data.sql
```

### Error: "Connection refused"

**Problem:** Cannot connect to Railway PostgreSQL

**Checklist:**
- ✅ Railway PostgreSQL service is running (check dashboard)
- ✅ Connection string is correct (no typos)
- ✅ No firewall blocking port 5432
- ✅ Credentials are current (Railway rotates them sometimes)

**Solution:**

```bash
# Get fresh connection string from Railway dashboard
# Test connection first:
psql "postgresql://postgres:PASSWORD@host:5432/railway" -c "SELECT version();"
```

### Error: "Permission denied"

**Problem:** Wrong user or insufficient privileges

**Solution:**

```bash
# Use the Railway-provided postgres user, not your local hlj_user
# Connection string should look like:
postgresql://postgres:RAILWAY_PASSWORD@host:5432/railway
#          ^^^^^^^^ This should be 'postgres'
```

### Error: "Duplicate key violation"

**Problem:** Data already exists in Railway table

**Solutions:**

```bash
# Option 1: Clear existing data first
railway connect postgres -c "TRUNCATE TABLE personality_quips CASCADE;"

# Option 2: Use ON CONFLICT (if you have unique constraints)
# Modify your export:
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  | sed 's/INSERT INTO/INSERT INTO/g' \
  | sed 's/VALUES/VALUES/g' \
  > quips_with_conflict.sql

# Then manually add ON CONFLICT DO NOTHING to each INSERT
```

### Data Looks Corrupted

**Problem:** Special characters, quotes, or encoding issues

**Solution:**

```bash
# Use UTF-8 encoding explicitly
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  --encoding=UTF8 \
  > personality_quips_utf8.sql

# Import with encoding specified
psql "$DATABASE_URL" \
  -f personality_quips_utf8.sql \
  --set=client_encoding=UTF8
```

---

## Keeping Quips in Sync

### One-Time Sync Script

Create `sync_quips_to_railway.sh`:

```bash
#!/bin/bash

# Sync personality_quips from local to Railway
# Usage: ./sync_quips_to_railway.sh

set -e  # Exit on error

echo "🎭 Syncing personality quips to Railway..."

# Export latest quips
echo "📦 Exporting from local database..."
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  > /tmp/quips_latest.sql

# Count local quips
LOCAL_COUNT=$(psql -U hlj_user -d highlowjack -t -c "SELECT COUNT(*) FROM personality_quips;")
echo "✅ Exported $LOCAL_COUNT quips from local database"

# Clear Railway table
echo "🧹 Clearing Railway table..."
railway connect postgres -c "TRUNCATE TABLE personality_quips CASCADE;"

# Import to Railway
echo "☁️  Importing to Railway..."
railway connect postgres < /tmp/quips_latest.sql

# Verify import
RAILWAY_COUNT=$(railway connect postgres -t -c "SELECT COUNT(*) FROM personality_quips;")
echo "✅ Imported $RAILWAY_COUNT quips to Railway"

# Compare counts
if [ "$LOCAL_COUNT" -eq "$RAILWAY_COUNT" ]; then
  echo "🎉 SUCCESS! Quips synced perfectly!"
else
  echo "⚠️  WARNING: Count mismatch! Local: $LOCAL_COUNT, Railway: $RAILWAY_COUNT"
  exit 1
fi

# Cleanup
rm /tmp/quips_latest.sql
echo "🧹 Cleaned up temporary files"
echo "✨ Sync complete!"
```

Make it executable:

```bash
chmod +x sync_quips_to_railway.sh
```

Run it:

```bash
./sync_quips_to_railway.sh
```

### Version Control Approach

**Keep quips in Git for deployment pipeline:**

```bash
# Export to version-controlled SQL file
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  > database/seeds/personality_quips.sql

# Commit to Git
git add database/seeds/personality_quips.sql
git commit -m "Updated personality quips"
git push

# In Railway deployment (add to deploy script):
railway connect postgres < database/seeds/personality_quips.sql
```

### Incremental Updates

**Add new quips without clearing existing:**

```sql
-- Export only new quips (after a certain date)
SELECT 
  'INSERT INTO personality_quips (player_name, trigger_event, quip_text, weight) VALUES (' ||
  '''' || player_name || ''', ' ||
  '''' || trigger_event || ''', ' ||
  '''' || replace(quip_text, '''', '''''') || ''', ' ||
  weight || ') ON CONFLICT DO NOTHING;'
FROM personality_quips
WHERE created_at > '2026-04-01'
ORDER BY created_at;
```

---

## Best Practices

### 1. **Always Backup Before Syncing**

```bash
# Backup Railway data before overwriting
railway connect postgres -c \
  "COPY personality_quips TO STDOUT WITH CSV HEADER" \
  > railway_quips_backup_$(date +%Y%m%d).csv
```

### 2. **Use Transactions for Safety**

```sql
-- Wrap imports in transaction
BEGIN;
TRUNCATE TABLE personality_quips CASCADE;
\i personality_quips_data.sql
-- Review changes before committing
SELECT COUNT(*) FROM personality_quips;
COMMIT;  -- Or ROLLBACK if something looks wrong
```

### 3. **Version Your Data Exports**

```bash
# Include date in export filename
pg_dump -U hlj_user -d highlowjack \
  --table=personality_quips \
  --data-only \
  --column-inserts \
  > personality_quips_$(date +%Y%m%d).sql
```

### 4. **Document Your Quips**

Keep a README with your quips:

```markdown
# Personality Quips Catalog

## Preezbob
- Triggers: CUT_TWO (50 quips), CUT_ACE (20 quips), WIN_MATCH (15 quips)
- Personality: Sardonic, self-aware about the curse

## Kreep  
- Triggers: STEAL_DEAL (30 quips), WIN_CLUTCH (25 quips)
- Personality: Competitive, trash-talking

## The Ballie
- Triggers: ALL (100+ quips)
- Personality: Wholesome, enthusiastic

Last updated: 2026-04-09
Total quips: 487
```

### 5. **Test in Staging First**

```bash
# If you have a staging Railway environment:
railway link --environment staging
railway connect postgres < personality_quips_data.sql

# Test
# Then promote to production:
railway link --environment production
railway connect postgres < personality_quips_data.sql
```

### 6. **Monitor After Sync**

```sql
-- After syncing, verify quip distribution
SELECT 
  player_name,
  trigger_event,
  COUNT(*) as quip_count,
  AVG(weight) as avg_weight
FROM personality_quips
GROUP BY ROLLUP (player_name, trigger_event)
ORDER BY player_name, trigger_event;
```

---

## Quick Reference

### Essential Commands

```bash
# Export from local
pg_dump -U hlj_user -d highlowjack --table=personality_quips --data-only --column-inserts > quips.sql

# Import to Railway (Railway CLI)
railway connect postgres < quips.sql

# Import to Railway (Direct)
psql "postgresql://postgres:PASSWORD@host:5432/railway" -f quips.sql

# Verify count
railway connect postgres -c "SELECT COUNT(*) FROM personality_quips;"

# View sample
railway connect postgres -c "SELECT * FROM personality_quips LIMIT 10;"
```

### Connection String Format

```
postgresql://USER:PASSWORD@HOST:PORT/DATABASE

Example:
postgresql://postgres:abc123xyz@containers-us-west-456.railway.app:5432/railway
```

### Common psql Commands

```sql
\dt                          -- List all tables
\d personality_quips         -- Describe table structure
\q                           -- Quit psql
\i /path/to/file.sql         -- Execute SQL file
\COPY table FROM 'file.csv'  -- Import CSV
```

---

## Appendix: Table Schema

For reference, the `personality_quips` table schema:

```sql
CREATE TABLE personality_quips (
    id BIGSERIAL PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    trigger_event VARCHAR(50) NOT NULL,
    quip_text TEXT NOT NULL,
    weight INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quips_player_trigger ON personality_quips(player_name, trigger_event);
```

---

## Support

**Issues?**
- Check Railway status: https://status.railway.app
- Railway docs: https://docs.railway.app
- PostgreSQL docs: https://www.postgresql.org/docs/

**Need help?**
- Verify connection: `railway connect postgres -c "SELECT version();"`
- Check logs: Railway dashboard → PostgreSQL → Logs
- Test with simple query first: `SELECT 1;`

---

**Built with 💚 by Primus & Dale**  
*April 9, 2026 - Quips to the Cloud!*

---

## Changelog

**v1.0.0** (2026-04-09)
- Initial release
- Four complete methods documented
- Troubleshooting guide included
- Sync scripts and best practices added
