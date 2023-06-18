@set current_ver=1.0-BETA
@set prev_ver=1.0.0
@set file_prefix=emogg-
@set file_postfix=.jar

@set current_file_name="%file_prefix%%current_ver%%file_postfix%"
@set prev_file_name="%file_prefix%%prev_ver%%file_postfix%"

@del "%appdata%\.minecraft\mods\%prev_file_name%"
@del "%appdata%\.minecraft\mods\%current_file_name%"

@copy ".\build\libs\%current_file_name%" "%appdata%\.minecraft\mods"
@echo Applied!
