# 一些常用公共的类和工具

- maven dependency (jdk1.8+)

  ```xml
  <dependency>
      <groupId>com.github.chengyuxing</groupId>
      <artifactId>rabbit-common</artifactId>
      <version>3.2.16</version>
  </dependency>
  ```

## UUID 字段转换

`ValueUtils.adaptValue(UUID.class, value)` 和 `DataRow.toEntity()` 支持标准 36 位 UUID 字符串、无横线的 32 位十六进制字符串以及已有的 UUID 对象；大小写均可，非法格式会抛出异常，`null` 保持为 `null`。

```java
UUID id = ValueUtils.adaptValue(UUID.class, "550e8400e29b41d4a716446655440000");
// 550e8400-e29b-41d4-a716-446655440000
```

## 实体映射错误定位

`DataRow.toEntity()` 的值转换或 setter 调用失败时，抛出的 `IllegalStateException` 包含实体类、属性名、来源列名、来源值类型、目标类型和 setter 名称，原始异常保留在 `getCause()`。`DataRow.ofEntity()` 的 getter 调用失败也会说明实体、属性、列名及 getter，并保留 getter 抛出的原始原因。

`ValueUtils.adaptValue()` 直接调用时仍保持原有的转换异常；映射实体时可通过 `getCause()` 判断日期解析、UUID 格式、自定义转换或访问方法中的错误。

## MostDateTime

`MostDateTime.of(String)` 保留从文本中提取日期的能力，例如 `决定书二〇〇一年十二月二十一日的`。验证完整输入时使用 `MostDateTime.parse(String)`；非法日期、未识别的前后缀和多余的小数秒位数会报错，不会自动修正日期。`ValueUtils.adaptValue` 的日期及时间字符串转换使用这个完整校验入口。

```java
MostDateTime value = MostDateTime.parse("2026-10-09T12:34:56.123456789Z");
value.toLocalDateTime(); // 不包含时区
value.getZonedDateTime(); // 包含时区
value.toInstant();
```

支持 ISO/RFC、紧凑数字日期、中文日期和时间；小数秒支持 1～9 位。`of(datetime, pattern)` 按指定格式严格解析，保留格式中的时区和偏移，不将引号中的格式字母当成时间字段。

SQL 风格的日期时间也支持用空格分隔日期与时间，并保留尾部的偏移量，例如 `2026-10-10 10:35:57.672+08`、`2026-10-10 10:35:57.672+0800`、`2026-10-10 10:35:57.672+08:30`。偏移量前可以有一个空格；秒数可省略，例如 `2026-10-10 10:35 +08`。`of` 从文本中提取此类时间时也保留完整偏移，不会将 `+08:30` 截成 `+08`。

`ValueUtils.adaptValue` 使用 `parse`，统一兼容已知的日期时间格式，并完整校验输入；解析失败不会退回 `of` 忽略多余文本。业务需要从描述性文本中提取时间时，应显式调用 `of`。非法日期和偏移量仍会报错。

不传时区的 `of(Temporal)` 保留输入已有的时区；`of(Temporal, ZoneId)` 将同一时刻转换到目标时区，无时区的本地日期和时间则在目标时区解释。纯时间使用其所属时区的今天，缺少年份使用当前年份。RFC-like 字符串中的 `CST` 有歧义，为兼容既有行为仍使用系统默认时区；需要确定时区时请使用明确偏移或区域时区。

旧的无参 `toZonedDateTime()` 实际返回 `LocalDateTime`，已更名为 `toLocalDateTime()`，旧调用需要修改。需要带时区的结果时使用 `getZonedDateTime()`；静态的 `MostDateTime.toZonedDateTime(String)` 仍提供文本提取。

`java.util.Date` 字段转换会得到普通 `Date`，精度为毫秒；`Timestamp` 和 Java 时间类型保留支持的纳秒精度。JDBC 日期和时间可以安全转换，SQL 日期转 `LocalDate`、SQL 时间转 `LocalTime` 保留日历值，其余转换按 epoch 时间值和指定时区处理。

## Tree

`com.github.chengyuxing.common.tree.Tree` 是不依赖 UI、数据库或业务模型的树状数据工具。泛型约束为 `Tree<T extends TreeNode<T, ID>, ID>`，保留前端 Tree 的节点自身类型与 ID 类型约束；Java 版对输入校验和结构修改采用更严格的规则。

节点实现 `TreeNode<T, ID>`，提供以下属性的 getter/setter：

| 属性 | Java 类型 | 说明 |
| --- | --- | --- |
| `id` | `ID` | 非空、唯一的节点标识，0 和空字符串也可以作为 ID |
| `pid` | `ID` | 父节点标识，只有 null 表示根节点 |
| `children` | `List<T>` | 子节点 |
| `parent` | `T` | 父节点引用 |

例如 `Node implements TreeNode<Node, Long>`，并具有 `String label` 实例字段：

```java
Tree<Node, Long> tree = new Tree<>(flatNodes);
List<Node> roots = tree.getRoots();
Node node = tree.getNode(2L); // 不存在时为 null
List<Long> matched = tree.search(n -> n.getId().equals(2L));
List<Long> context = tree.searchWithContext(n -> n.getId().equals(2L));
tree.addNode(1L, newNode); // pid 为 null 时添加根节点，支持携带子树
tree.updateNode(2L, Collections.singletonMap("label", "updated"));
tree.moveNode(2L, null); // 将节点及其子树移动到根节点列表末尾
List<Node> path = tree.getPath(2L); // 从根或最高已知祖先到自身
tree.getAncestors(2L);
tree.getDescendants(1L);
tree.getSiblings(2L); // 包含自身；孤立节点延续原工具的根节点列表规则
tree.getSibling(2L, Tree.SiblingPosition.NEXT);
Tree<Node, Long> filtered = tree.filterTree(n -> n.getId().equals(2L));
List<Node> flat = tree.toFlat();
tree.removeNode(2L); // 连同后代一起从树及索引移除
```

- 构建时浅复制输入节点，包括继承的实例字段；已有 `children`、`parent` 不参与构建，原始节点不被修改。默认节点类需有无参构造器；无无参构造器时可使用 `new Tree<>(flatNodes, Node::new)` 提供复制构造器，或传入自己的复制函数，复制节点字段并返回新对象。
- 构建拒绝空节点、空 ID、重复 ID、自指及父链成环。缺失父节点的数据仍留在索引中，不会自动成为根节点；`getSize()` 包含这些孤立节点，`isEmpty()` 和 `getRootCount()` 以根节点为准。
- `addNode` 添加传入节点本身，使用其 `children` 构建子树，并重设 `pid`、`parent`、`children`。先校验整个新增子树：父节点不存在、空 ID、与已有节点或子树内重复的 ID、子树成环时抛出 `IllegalArgumentException`，数据校验失败不修改传入节点和已有树。
- `updateNode` 只更新业务实例字段，字段须存在且值类型兼容。禁止修改 `id`、`pid`、`children`、`parent`；其他名称的字段若改变结构也会被拒绝并恢复该字段。变更父节点使用 `moveNode`，新增和删除使用对应方法。
- `moveNode(id, pid)` 移动整个子树到新父节点的子列表末尾；`pid` 为 null 时移到根列表末尾。节点或父节点不存在、移动到自身或后代时抛出异常，保持原结构。移到当前父节点不改变兄弟顺序，节点对象及 ID 索引保持不变。
- `getPath` 返回从根节点或孤立子树最高已知祖先到当前节点自身的路径；节点不存在时返回空列表。路径列表是新列表，列表中的节点属于原树。
- `filterTree` 逐个判断所有索引节点，仅保留命中节点及其祖先；命中父节点不会自动保留后代。返回节点副本构成的新树，父子结构独立，业务字段按配置的复制函数复制，默认仍是浅复制。保留移动后的根和兄弟顺序；新树索引按根开始的深度优先顺序、随后孤立子树顺序插入。命中的孤立节点及其祖先仍留在新树索引中，不自动变成根节点。
- `search` 返回索引插入顺序的命中 ID，也能找到孤立节点；`searchWithContext` 按命中顺序收集自身、从近到远的祖先和深度优先的后代，并按首次出现去重。
- `toFlat` 按根节点开始的深度优先顺序返回浅复制节点，不包含孤立节点，`parent`、`children` 设为 null。新增子树、后代查询、过滤、展开和删除使用迭代遍历，支持深层结构。
- `getRoots`、`getNode`、`getSiblings` 返回树内对象或列表。结构修改应通过树的方法完成；直接改返回节点的 ID、父节点或结构列表，不会自动同步索引。此类不是线程安全的。
- `SiblingPosition` 的 `NEXT`、`PREVIOUS`、`FIRST`、`LAST` 对应前端的四种位置，越界或节点不存在时返回 null。
