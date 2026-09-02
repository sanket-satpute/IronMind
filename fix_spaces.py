import sys

def fix_spaces(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    result = []
    i = 0
    n = len(content)
    
    IN_NORMAL = 0
    IN_STRING = 1
    IN_TRIPLE = 2
    state = IN_NORMAL
    escape = False
    
    while i < n:
        if state == IN_NORMAL:
            if content[i:i+3] == '\"\"\"':
                state = IN_TRIPLE
                result.append('\"\"\"')
                i += 3
                continue
            elif content[i] == '\"':
                state = IN_STRING
                result.append('\"')
                i += 1
                continue
            
            if content[i] == ' ':
                space_count = 0
                j = i
                while j < n and content[j] == ' ':
                    space_count += 1
                    j += 1
                
                if space_count >= 4 and len(result) > 0 and result[-1] not in ('\n', '\r'):
                    result.append('\n')
                    result.append(' ' * space_count)
                    i = j
                    continue
                else:
                    result.append(' ' * space_count)
                    i = j
                    continue
            
            result.append(content[i])
            i += 1
            
        elif state == IN_STRING:
            if escape:
                result.append(content[i])
                escape = False
                i += 1
            elif content[i] == '\\':
                escape = True
                result.append(content[i])
                i += 1
            elif content[i] == '\"':
                state = IN_NORMAL
                result.append(content[i])
                i += 1
            else:
                result.append(content[i])
                i += 1
                
        elif state == IN_TRIPLE:
            if content[i:i+3] == '\"\"\"':
                state = IN_NORMAL
                result.append('\"\"\"')
                i += 3
            else:
                result.append(content[i])
                i += 1

    with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
        f.write(''.join(result))

try:
    fix_spaces('app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/TaskBuilderScreen.kt')
    fix_spaces('app/src/main/java/com/sanket_satpute_20/ironmind/onboarding/AppSelectorScreen.kt')
    print('Spaces fixed!')
except Exception as e:
    print('Error:', e)
