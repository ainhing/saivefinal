import os
import re

java_dir = 'app/src/main/java/com/example/saive/admin'
java_files = []
for root, _, files in os.walk(java_dir):
    for file in files:
        if file.endswith('.java'):
            java_files.append(os.path.join(root, file))

hardcoded_java = []
pattern = re.compile(r'(showCustomToast|setText|setTitle|setMessage|Toast\.makeText[^;]+?|Log\.[diwev])\s*\(\s*"([^"\\]+?)"\s*\)')

for java_file in java_files:
    with open(java_file, 'r', encoding='utf-8') as f:
        for i, line in enumerate(f):
            matches = pattern.findall(line)
            for match in matches:
                hardcoded_java.append((os.path.basename(java_file), i + 1, match[1]))

with open('hardcoded_java_results.txt', 'w', encoding='utf-8') as out:
    for item in hardcoded_java:
        out.write(f'{item[0]}:{item[1]} - {item[2]}\n')
    out.write(f'Total hardcoded strings in Java: {len(hardcoded_java)}\n')
