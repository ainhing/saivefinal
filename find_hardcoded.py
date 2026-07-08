import os
import re
import glob

layout_dir = 'app/src/main/res/layout'
admin_xmls = glob.glob(os.path.join(layout_dir, 'admin_*.xml'))

hardcoded = []
pattern = re.compile(r'android:(text|hint)="([^@].*?)"')

for xml_file in admin_xmls:
    with open(xml_file, 'r', encoding='utf-8') as f:
        for i, line in enumerate(f):
            matches = pattern.findall(line)
            for match in matches:
                hardcoded.append((os.path.basename(xml_file), i + 1, match[1]))

with open('hardcoded_results.txt', 'w', encoding='utf-8') as out:
    for item in hardcoded:
        out.write(f'{item[0]}:{item[1]} - {item[2]}\n')
    out.write(f'Total hardcoded strings in XML: {len(hardcoded)}\n')
