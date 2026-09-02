import os
import re

base_dir = r"C:\Users\lenovo\AndroidStudioProjects\IronMind\app\src\main\java\com\sanket_satpute_20\ironmind"

def replace_in_file(path, replacements):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = content
    for pattern, repl in replacements:
        new_content = re.sub(pattern, repl, new_content)
        
    if new_content != content:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {path}")

replacements = [
    # SensoryFeedbackEngine -> HapticsManager / SoundManager
    (r'com\.sanket_satpute_20\.ironmind\.utils\.SensoryFeedbackEngine\.triggerSuccess\((.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(\1).playTaskComplete()'),
    (r'SensoryFeedbackEngine\.triggerSuccess\((.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playSuccess(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(\1).playTaskComplete()'),
    
    (r'com\.sanket_satpute_20\.ironmind\.utils\.SensoryFeedbackEngine\.triggerPunishment\((.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playError(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(\1).playTemptationBlocked()'),
    (r'SensoryFeedbackEngine\.triggerPunishment\((.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playError(); com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(\1).playTemptationBlocked()'),
    
    (r'com\.sanket_satpute_20\.ironmind\.utils\.SensoryFeedbackEngine\.triggerHeartbeat\((.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playTick()'),
    (r'SensoryFeedbackEngine\.triggerHeartbeat\((.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playTick()'),
     
    (r'com\.sanket_satpute_20\.ironmind\.utils\.SensoryFeedbackEngine\.triggerShieldHaptic\((.*?), (.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playError()'),
    (r'SensoryFeedbackEngine\.triggerShieldHaptic\((.*?), (.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.HapticsManager.getInstance(\1).playError()'),
     
    (r'com\.sanket_satpute_20\.ironmind\.utils\.SensoryFeedbackEngine\.playShieldSound\((.*?), (.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(\1).playTemptationBlocked()'),
    (r'SensoryFeedbackEngine\.playShieldSound\((.*?), (.*?)\)', 
     r'com.sanket_satpute_20.ironmind.gamification.SoundManager.getInstance(\1).playTemptationBlocked()'),

    # Remove imports
    (r'import com\.sanket_satpute_20\.ironmind\.utils\.SensoryFeedbackEngine\n', r''),
]

for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith(".kt") and file != "SensoryFeedbackEngine.kt":
            path = os.path.join(root, file)
            replace_in_file(path, replacements)
