java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512m -Xmx512M -Xss2M -noverify -javaagent:/home/mjg/tools/jrebel.jar -jar launcher.jar "$@"
