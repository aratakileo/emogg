语言: [[English]](https://github.com/aratakileo/emogg) | [[Русский]](preview/README_ru.md) | [[中文]](preview/README_cn.md)

# Emogg，为了更好的聊天体验！<img src="https://github.com/aratakileo/static.pexty.xyz/blob/main/src/emoji/animated/minecraft.gif?raw=true" height="35"/>
为 Minecraft 添加表情包功能。

![](preview/preview-1.gif)
![](preview/preview-2.gif)

<p align="center">
 <a href="https://discord.gg/t5ZqftXG4b">
    <img src="https://img.shields.io/badge/Discord-7289da?style=for-the-badge&logo=discord&logoColor=ffffff" alt="Discord" />
  </a>
</p>

### 功能
- 支持静态和动画表情
- 通过资源包添加自定义表情：只需将表情图片放在资源包的`assets/emogg/emoji`文件夹中。  
  也可以通过[我们的网站](https://aratakileo.github.io/emogg-resourcepack-maker/)将 Discord 表情转换成资源包。
- 表情会在聊天，告示牌，物品名，实体名，以及容器标题等中显示（暂时还不能支持一些纯文本）
- 表情提示和自动补全
- 表情选择菜单
- 两个内置表情包

### 使用
要输入表情，只需输入`:表情名称:`（中英文冒号皆可）。在聊天界面中会出现补全提示。
与可以点击聊天界面右下角的按钮（或者鼠标中键以及`Shift`+`Esc`）来打开表情选择界面。
你还可以点击聊天以及书中的表情字符以复制其名称。

在多人游戏中，其他玩家需要安装Emogg以及对应的的资源包以显示对应的表情，如果一个表情在客户端中不存在则会显示为`:表情名称:`

如果你不希望某个表情被显示，可以通过`\:表情名称:`的方式转义。

### 添加自定义表情
[我们的网站](https://aratakileo.github.io/emogg-resourcepack-maker/)可以自动用 Discord 表情生成Emogg资源包。 

要通过资源包添加自定义表情，只需要将对应的图片（`.png`或`.gif`）放在资源包的`assets/emogg/emoji`文件夹内。

表情的分辨率在`128x128`左右比较合适（更高的分辨率没有什么必要），但任何的分辨率都是支持的。
表情本身尽量填满整个图片，并且不建议有很小的细节。
非正方形的图片也是支持的，这些表情会被自动缩放到合适的大小来适应文字的高度。

图片的文件名（不包括扩展名）会成为表情名称。表情名称只能包含拉丁字母，数字，以及下划线，因此会进行如下变换：
- 所有空格，点和横线会被替换为下划线
- 收尾空格会被去掉
- 所有不支持的字符都会被去掉
- 大写拉丁字母会被转换为小写

这些规则对表情类别名称也适用。默认情况下，自定义表情会被添加到`其他`类别。
但可以通过把表情图片放在`emoji`文件夹的某个子文件夹中来自定义每个表情的类别。
文件夹的名称会作为该类别的名字。类别类似于一个命名空间，如果多个资源包中包含同名类别则它们添加到这个类别的表情都会被归为一类。  
模组默认包含以下类别（注意在资源包文件夹名称要用括号中的英文名称）：
- `动漫`（`anime`）
- `梗`（`memes`）
- `人`（`people`）
- `自然`（`nature`）
- `食物`（`food`）
- `活动`（`activities`）
- `旅行`（`travel`）
- `物品`（`objects`）
- `符号`（`symbols`）
- `旗帜`（`flags`）
- `其他`（`other`）

通过资源包添加的自定义类别默认不会有翻译，
但可以通过添加`emogg.category.类别名`的词条来添加翻译。

如果加载表情的时候出现同名表情，则表情名称末尾会被自动加上下划线编号以区分它们。

至于如何制作资源包可以很容易在网上查到，你也可以在这里下载一个[模组内置的资源包](https://github.com/aratakileo/emogg/raw/main/resourcepack/builtin.zip)来作为示例。
