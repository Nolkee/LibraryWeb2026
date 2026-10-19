#!/usr/bin/env python3
"""Convert supplied Navicat record-separated backup to SQL; never invent sample data."""
import gzip,json,pathlib,sys
root=pathlib.Path(sys.argv[1] if len(sys.argv)>1 else '../LibraryDB')
print('CREATE DATABASE IF NOT EXISTS LibraryWeb2026 CHARACTER SET utf8mb4; USE LibraryWeb2026; SET FOREIGN_KEY_CHECKS=0; SET NAMES utf8mb4;')
for path in sorted(root.glob('*.meta.json.gz')):
 m=json.loads(gzip.open(path,'rt').read())
 if m.get('Type')!='Table':continue
 print(m['DDL']+';')
 cols=','.join('`'+v+'`' for v in m['Fields'])
 for data in m.get('Data',[]):
  raw=gzip.open(root/data['Filename'],'rt').read()
  for row in raw.split('\x1e'):
   row=row.strip()
   if row:print('INSERT INTO `'+m['Name']+'` ('+cols+') VALUES '+row+';')
print('SET FOREIGN_KEY_CHECKS=1;')
