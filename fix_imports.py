import os
import re

tokens = [
    "DeepBackground", "SurfaceDark", "SurfaceElevated", 
    "NeonCyan", "IronRed", "ElectricViolet", "GoldXP", 
    "SuccessGreen", "ErrorRed", "WarningAmber", "TextPrimary"
]

def fix_imports():
    base_dir = r"C:\Users\lenovo\AndroidStudioProjects\IronMind\app\src\main\java\com\sanket_satpute_20\ironmind"
    fixed_count = 0
    
    for root, dirs, files in os.walk(base_dir):
        if "ui\\theme" in root or "ui/theme" in root:
            continue
            
        for file in files:
            if file.endswith(".kt"):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                needed = [t for t in tokens if re.search(r'\b' + t + r'\b', content)]
                if not needed:
                    continue
                    
                missing = [t for t in needed if f"import com.sanket_satpute_20.ironmind.ui.theme.{t}" not in content]
                
                if missing:
                    lines = content.splitlines()
                    # find package declaration or first import
                    insert_idx = 0
                    for i, line in enumerate(lines):
                        if line.startswith("package "):
                            insert_idx = i + 1
                            break
                    
                    for t in missing:
                        lines.insert(insert_idx, f"import com.sanket_satpute_20.ironmind.ui.theme.{t}")
                        insert_idx += 1
                        
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write('\n'.join(lines))
                    fixed_count += 1
                    print(f"Fixed imports in {file}: {missing}")
                    
    print(f"Total files fixed: {fixed_count}")

if __name__ == '__main__':
    fix_imports()
