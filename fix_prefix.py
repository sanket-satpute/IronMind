import os
import re

tokens = [
    "DeepBackground", "SurfaceDark", "SurfaceElevated", 
    "NeonCyan", "IronRed", "ElectricViolet", "GoldXP", 
    "SuccessGreen", "ErrorRed", "WarningAmber", "TextPrimary"
]

def fix_prefixes():
    base_dir = r"C:\Users\lenovo\AndroidStudioProjects\IronMind\app\src\main\java\com\sanket_satpute_20\ironmind"
    fixed_count = 0
    
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith(".kt"):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                new_content = content
                for t in tokens:
                    # Fix androidx.compose.ui.graphics.Token -> Token
                    new_content = new_content.replace(f"androidx.compose.ui.graphics.{t}", t)
                    
                    # Fix Color.Token -> Token in case the script matched Color.something
                    # Actually script matched Color(0x...). It might be preceded by android.graphics.Color or androidx.compose.ui.graphics.
                    new_content = new_content.replace(f"android.graphics.{t}", t)
                    
                if new_content != content:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    fixed_count += 1
                    print(f"Fixed prefix in {file}")
                    
    print(f"Total files prefix fixed: {fixed_count}")

if __name__ == '__main__':
    fix_prefixes()
