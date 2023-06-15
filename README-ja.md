# 免責事項
- このコンテンツはJavaやセキュリティに関する教育目的で作成されています。万が一、データの損失に直面したり、法律に接触したとしても、開発者は一切責任を負いかねますので、使用する際は自己責任でお願いします。

# 警告
- 適用後はAPKのサイズが2倍になります。
- 改造APKを開いた後、元のAPKのサイズの空きストレージが消費されます。

# 必要な環境
- Java（元のAPKの署名確認用）
- Android Studio または AIDE（中心となるAPKのビルド用）

# 注入方法
- APKSignReader フォルダに入っている how-to-use-ja.txt に従い、APKSignReader.jar を使って元のAPKの署名データを読み取り、Javaソースコードの`com.SignatureKiller.Main`クラスの`Hook`メソッド内の`SignData`変数にコピペします。
- Android Studio または AIDE でビルドします。
- ビルドしたAPK内のDEXファイルを改造APKに追加します。
- 元のAPKをorig.apkに改名し、改造APKのassetsフォルダにコピーします。（フォルダがない場合は作成してください。）
- 以下のsmaliコードを、第一引数がContextまたはそれを継承したクラスオブジェクトであるエントリポイントのメソッドのトップに貼り付けてください。（onCreate, attachBaseContext, など）
```smali
invoke-static {p0}, Lcom/SignatureKiller/Main;->Hook(Landroid/content/Context;)V
````
- 改造APKを署名してインストールしたら、署名認証に合格するか確認してください。
