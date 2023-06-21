from os import listdir

print("Start process!")

emoji_dir_path = 'src/main/resources/assets/emogg/emoji/'
builtin_emoji_paths = []

amount = 0
for emoji_path in listdir(emoji_dir_path):
    if emoji_path.endswith('.gif') or emoji_path.endswith('png'):
        builtin_emoji_paths.append(f'emoji = Emoji.of(new ResourceLocation('
                                   f'Emogg.NAMESPACE,'
                                   f'"{"emoji/" + emoji_path}"'
                                   f'));\n'
                                   f'builtinEmojis.put(emoji.getName(), emoji);')

        amount += 1

print(f"Defined: {amount}")
print('\n'.join(builtin_emoji_paths))
