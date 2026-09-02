#!/usr/bin/env python3
import os
import re

GAMIFIED_CLICK_IMPORT = "import com.sanket_satpute_20.ironmind.gamification.gamifiedClick"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content
    modified = False

    # Replace .clickable { with .gamifiedClick {
    if '.clickable {' in content or '.clickable ' in content:
        content = re.sub(r'\.clickable\s*\{', '.gamifiedClick {', content)
        content = re.sub(r'\.clickable\s*\(\s*onClick\s*=\s*\{', '.gamifiedClick {', content)
        if GAMIFIED_CLICK_IMPORT not in content and '.gamifiedClick' in content:
            content = re.sub(r'(package [^\n]+)\n', r'\1\n\n' + GAMIFIED_CLICK_IMPORT + '\n', content)
        modified = True

    if modified and content != original_content:
        with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)
        print(f"Updated {filepath}")
        return True
    return False

def main():
    base_dir = "C:/Users/lenovo/AndroidStudioProjects/IronMind/app/src/main/java/com/sanket_satpute_20/ironmind"
    updated_count = 0
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith(".kt"):
                if process_file(os.path.join(root, file)):
                    updated_count += 1
    
    print(f"Total files updated: {updated_count}")

if __name__ == '__main__':
    main()
