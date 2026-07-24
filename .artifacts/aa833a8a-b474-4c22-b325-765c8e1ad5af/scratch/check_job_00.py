import sqlite3
import os

db_path = "E:/AndroidStudioProjects/AV7BibleAppv4/app/src/main/assets/newDb"
if not os.path.exists(db_path):
    print("DB not found in assets")
else:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT verse, text FROM Bible WHERE Book = 'JOB' AND chapter = '00' ORDER BY verse+0")
    rows = cursor.fetchall()
    for row in rows:
        print(f"Verse {row[0]}: {row[1][:50]}...")
    conn.close()
