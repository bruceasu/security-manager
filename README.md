# 普通單次運行程序的虛擬機參數參考
一段快速執行，關閉了JIT的腳本，啟動命令大概長這個樣子：
-Xms96m -Xmx96m -Xmn64m -Xss256k -XX:+UseSerialGC -Djava.compiler=NONE -Xverify:none -XX:AutoBoxCacheMax=20000

一段輔助程序，啟動命令大概長這個樣子
-Xms256m -Xmx256m -XX:NewRatio=1 -Xss256k -XX:+UseSerialGC -XX:-TieredCompilation -XX:CICompilerCount=2 -Xverify:none -XX:AutoBoxCacheMax=20000
