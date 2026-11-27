# Lab Notes

## 24 Nov 2026: Lesson 3, Service Builder

(The evening session ran past midnight into 25 Nov.)

### Done

- **Entity `Task`** in `modules/task` (`task-api`, `task-service`), package `com.acme.task`, namespace `ACME`, `uuid="true"`, `remote-service="false"`. Finders `GroupId` and `G_S`, ordered by `dueDate` asc.
- **Custom methods** in `TaskLocalServiceImpl`: `addTask(...)` and `getTasksByStatus(groupId, status)`.
- **Service Builder forced to 1.0.496** to match the GA132 kernel (see errors 2 and 3).
- **Step 2b:** model hint `max-length` 4000 for `description`. `tables.sql` now says `description STRING null`.
- **Deployed to CE 7.4 GA132:**
  - `STARTED com.acme.task.api_1.0.0` and `STARTED com.acme.task.service_1.0.0`
  - Gogo `lb task`: both Active. `scr:info com.acme.task.service.impl.TaskLocalServiceImpl`: State ACTIVE.
- **Table `acme_task`** in `lportal74`:

  | Column | Type |
  |---|---|
  | `uuid_` | varchar(75) |
  | `taskid` | bigint, primary key |
  | `groupid`, `companyid`, `userid` | bigint |
  | `username`, `title` | varchar(75) |
  | `description` | **text** (after the hint and the table rebuild) |
  | `status` | integer |
  | `createdate`, `modifieddate`, `duedate` | timestamp |

  Indexes: `(groupid, status)` and UNIQUE `(uuid_, groupid)`.
- **`release_`:** `com.acme.task.service | 1.0.0`. There is no row for `task-api`, because it has no tables.
- **Script Console:** created task ID 1. "Open tasks in Guest site: 1". The row in Postgres has status 0, and `uuid_` was filled automatically.
- **Commits on `ce-7.4`, both pushed:**
  - `bc6405d` Lesson 3: Task entity with Service Builder (before model hints)
  - `7a35179` Lesson 3: description max-length 4000 (model hint)

### Errors and fixes

| Error (short) | Real cause | Fix | Time lost |
|---|---|---|---|
| `buildService` failed: `non-zero exit value 1` (earlier on 24 Nov) | A finder used a column that did not exist. Blade's sample columns `field1`..`field5` were still in `service.xml`. | Replaced all sample columns with the Task columns | not recorded |
| `TaskUtil` did not compile: `cacheResult(List)` (earlier on 24 Nov) | Workspace plugin 17.1.9 brings Service Builder 1.0.552, which generates code for newer kernels than GA132 | `serviceBuilder` version 1.0.496 plus `resolutionStrategy.force` in `task-service/build.gradle` | not recorded |
| Old `task-api/build.gradle` was staged in Git | A broken earlier version (the module depended on itself) was staged. The fixed file on disk was not staged. | `git add modules/task` again before the first commit | ~2 min |
| "BUILD SUCCESSFUL", but the module never reached CE | `liferay.workspace.home.dir` in `gradle-local.properties` pointed to `server/liferay-portal-7.4.3.132-ga132`, an empty folder. The real CE is `server/liferay-portal`. (Lesson learned 3 again.) | Corrected the path. Checked the log for `STARTED` after the deploy. | ~15 min |
| `description` stayed `varchar(75)` after the hint | I deployed **before** Step 2b. Liferay creates a table only once. A redeploy with a new hint does not change an existing table. | Lab fix: Gogo `stop 1395`, `DROP TABLE acme_task`, deleted the `release_` and `servicecomponent` rows, Control Panel → Server Administration → "Clear the database cache", Gogo `start 1395`. Liferay created the table again, with `description text`. | ~45 min |
| `blade sh` cannot connect on CE | CE does not listen on Gogo port 11311 (nothing configured in `portal-ext.properties`) | Used Control Panel → Configuration → Gogo Shell instead | ~5 min |
| Experiment E: the Impl comment was not tested at first | I did not save `TaskLocalServiceImpl.java` before running `buildService` | Saved the file, checked it with `grep`, then ran `buildService` again | ~5 min |
| `zsh: no matches found: --include=*.java` | zsh tries to expand `*.java` itself before `grep` runs. There are no `.java` files in the current folder, so zsh stops. | Put quotes around the pattern: `--include='*.java'` | ~5 min |

### Experiment results and my answers

**Step 2b (model hint)**
- **Prediction A:** "the column will be bigger, about 4,000 characters (VARCHAR(4000))."
- **Result:** `tables.sql` shows `description STRING null`, not `VARCHAR(4000)`. Service Builder's rule:
  - `max-length` < 4000 → `VARCHAR(n)`
  - exactly 4000 → `STRING`
  - more than 4000 → `TEXT`

  On PostgreSQL, `STRING` became `text`.
- **Prediction B** (does the existing column change after a redeploy?): I did not predict before running. **Result:** no change, still `varchar(75)`.
- **Prediction C** (column type after the table rebuild): `text`. **Correct.**

**Part 4.2: why `uuid_` has an underscore**
- **My answer:** "The UUID is created for all tables by default. It helps to recognize the row and helps in import and export. The underscore: I don't know."
- **Correction:**
  - `uuid_` exists only because `service.xml` has `uuid="true"`.
  - It identifies the same record across different databases (staging → production, LAR export/import). `taskId` changes between systems; the `uuid_` stays the same.
  - The `_` is added because `uuid` is a reserved word or a type name in some databases. The same rule gives `user_`, `group_` and `release_`.

**Experiment D: the `release_` table**
- `schemaversion` of `com.acme.task.service`: **1.0.0**
- `task-service/bnd.bnd`, line 4: `Liferay-Require-SchemaVersion: 1.0.0`
- `SELECT count(*) FROM release_;` → **199** rows. Each row is one module with its own schema version.
- **Why Liferay needs both,** my answer: "to recognize which Liferay and which module version we are using."
- **Correction:**
  - The header is what the **code needs**. The `release_` row is what the **database has**.
  - If the code needs a higher version than the database has, Liferay does not start the service until an upgrade step brings the schema up (Saturday: 1.0.0 → 1.1.0 for `priority`).

**Experiment E: the golden rule**
- **My prediction** (made after I saw the Base result): "the comments will be removed."
- **Result:**
  - The comment in `TaskLocalServiceBaseImpl.java` was **removed**. The file is generated again on every `buildService`.
  - The comment in `TaskLocalServiceImpl.java` **stayed**. Service Builder creates the Impl file only once and after that only reads it, to copy public methods into `TaskLocalService`.
- **Detail from the file times:** Service Builder saves a generated file only when its content changes. Every `buildService` also raises `build.number` in `service.properties`.

**Experiment F: generated SQL and javax**
- **Finder SQL for `G_S`:** `task.groupId = ? AND task.status = ?`. The full query is `SELECT task FROM Task task WHERE task.groupId = ? AND task.status = ? ORDER BY task.dueDate ASC`. This is HQL/JPQL (Java names), not plain SQL (table names).
- **javax inventory** (`modules/task`):

  | Package | Files | Generated? | Java EE or JDK |
  |---|---|---|---|
  | `javax.sql.DataSource` | `TaskPersistenceImpl.java`, `TaskLocalServiceBaseImpl.java` | yes | _(my answer: to do)_ |

  No `javax.portlet`, `javax.servlet` or `javax.ws.rs` in `modules/task`.

**Part 6: three questions in English:** _not written yet (see "Still to do")._

### Open questions for my tutor

1. **Was deleting the `servicecomponent` rows needed?** For the lab table rebuild I deleted three things: the table, the `release_` row and the `servicecomponent` rows. Then I cleared the database cache. After `start 1395`, Liferay created the table and a new `release_` row, but **no** new `servicecomponent` row. So is the PDF's way (table + `release_` only) enough? Is clearing the cache always needed? And can the missing `servicecomponent` row cause a problem on the next deploy?
2. **Step 2b in the PDF:** please confirm the rule above (`max-length` < 4000, = 4000, > 4000). The PDF only says "the column type changes".
3. **The Experiment F `grep` command** needs quotes in zsh, the default shell on macOS: `--include='*.java'`.
4. **CLAUDE.md needs updates:**
   - The CE folder is `server/liferay-portal`, not `server/liferay-portal-7.4.3.132-ga132`.
   - Gogo port 11311 is not open on CE. Should I enable it in `portal-ext.properties` (needs a restart), or keep using the Control Panel Gogo Shell?
   - Section 3 (progress) is out of date.

### Still to do

- [ ] Part 6: write the 3 answers in English (3–5 sentences each)
- [ ] Experiment F step 3: sort `javax.sql` (Java EE or JDK) and fill in the table above
- [ ] Experiment D check question: what do I write on Saturday to move the schema from 1.0.0 to 1.1.0?
- [ ] Part 8: send experiments A and B answers, the quiz, and the Experiment C answers. Write 5 lines of English notes.
- [x] Part 9: company code comparison (done by my tutor in the Lesson 4 PDF, Part 9)
- [x] Remove the Experiment E changes (already gone, checked on 25 Nov)
- [x] Clean CE: greeter JARs from Experiment C removed from `server/liferay-portal/osgi/modules`
- [x] Delete the empty folder `server/liferay-portal-7.4.3.132-ga132`
- [x] Update CLAUDE.md sections 2 and 3 (done by my tutor on 25 and 26 Nov)

## 25-27 Nov 2026: Lesson 4, MVC portlet `task-web`

**Status: closed by my decision on 27 Nov.** I move on to the next lesson. The code is written and
compiles, but it was **never deployed**, and the experiments were not done (see "Still to do").

### Done

- **Part 1.1 (lab cleanup):**
  - Gogo port: `module.framework.properties.osgi.console=localhost:11311` in CE `portal-ext.properties`. After a CE restart, port 11311 listens on `127.0.0.1`, and `blade sh "lb acme"` works on CE (`task-api` 1394 and `task-service` 1395 Active).
  - Greeter JARs removed from CE `osgi/modules`. The empty folder `server/liferay-portal-7.4.3.132-ga132` and the empty `~/Documents/Programming/Liferay/.git` are deleted.
- **Part 3.1:** `blade create -t mvc-portlet -p com.acme.task.web -c TaskWeb -d modules/task task-web`. The module sits next to `task-api` and `task-service` (`modules/task` is only a folder, it has no `bnd.bnd`).
- **Parts 3.1-3.5 (new way of working: VS Code Claude wrote the code from the PDF listings, I reviewed it):**
  - `build.gradle`: `compileOnly project(":modules:task:task-api")`
  - `Language.properties`: widget title "Tasks" and the keys of the PDF (Blade's `taskweb.caption` kept, now unused)
  - `ViewTasksMVCRenderCommand` (`/`, `/task/view_tasks`), `EditTaskMVCRenderCommand` (`/task/edit_task`), `AddTaskMVCActionCommand` (`/task/add_task`), `UpdateTaskStatusMVCActionCommand` (`/task/update_status`, **no permission check on purpose**)
  - `init.jsp` (5 imports added), `view.jsp` (replaced), `edit_task.jsp` (new)
  - `blade gw :modules:task:task-web:compileJava`: BUILD SUCCESSFUL. Not deployed.
- **Code review:** I traced the whole "Add Task" and "Mark Done" flow (4 requests: render `/` → render `/task/edit_task` → action `/task/add_task` → redirect, render `/`).
- **Company workspace scan** (private notes, not in this repo): which module types my company uses most.

### Errors and fixes

| Error (short) | Real cause | Fix | Time lost |
|---|---|---|---|
| Gogo port 11311 not listening after I added the console line | CE was started before the line was added. `portal-ext.properties` is read only at startup. | Restarted CE | ~5 min |
| `compileJSP` failed: `ClassNotFoundException: com.liferay.taglib.ui.TableIteratorTei` (line 1 of every JSP, also Blade's `init.jsp`) | The Gradle JSP compiler loads the `liferay-ui` taglib definition, but the Liferay taglib classes are not on its classpath in this workspace. A tooling limit, not a code error. | None. The server compiles the JSPs at runtime, so the real check is the browser and `catalina.out`. | ~5 min |

### Experiment results and my answers

**Review question 1: what if `"mvc.command.name=/"` is missing?**
- **My answer:** "the `/` will not show in the URL… it will not grab the values of tasks."
- **Correction:**
  - `mvc.command.name` is only a name (a string), not a path in the URL. The name `/` means "the command to use when the URL has no `mvcRenderCommandName`".
  - Without it, `MVCPortlet` falls back to `view-template=/view.jsp` and includes the JSP directly, so no `setAttribute` happens.
  - `view.jsp` line 5, `int status = (Integer)request.getAttribute("status")`, unboxes `null` → `NullPointerException`.

**Review question 2: what if `task-service` is stopped?**
- **My answer:** "all the dependent modules will stop and fail. I am not sure."
- **Correction:** OSGi works per component, not per bundle. `task-web` stays Active. `ViewTasks…`, `AddTask…` and `UpdateTaskStatus…` (with `@Reference TaskLocalService`) become **unsatisfied**. `EditTask…` and the portlet class stay active. Without the `/` command, the page gives the same NPE as in question 1.

**Review question 3 (link vs. form):** not answered.

**My flow trace (corrections):**
- `groupId` comes from `ThemeDisplay.getScopeGroupId()` (the current site), not from the service.
- The Save button has no URL. The **form** has it (`<aui:form action="<%= addTaskURL %>">`).
- `if (Validator.isNull(title))` is the **error** path: add the error, show the form again, `return`. Nothing is saved. Only a non-empty title reaches `addTask`.
- I missed the hidden `redirect` field (where the browser goes after a successful save) and the end of each flow (`INSERT` / `UPDATE acme_task`).
- Wording: a portlet looks for a **render command**. "Resource" is the name of the third phase.

**Part 1.2, Part 1.3, Part 3.6, Experiments G, H, I, J, Part 6:** not done (see "Still to do").

### Open questions for my tutor

1. **I closed Lesson 4 without the deploy.** Should Lesson 5 start with Part 4 (deploy `task-web`, add the widget, verify)? The permission lesson needs the widget running, and Experiment I is the reason for Lesson 5.
2. **`compileJSP` does not work in this workspace** (see Errors). Is there a simple way to put the Liferay taglib classes on its classpath, so I can check JSPs before a deploy? That would be useful in Week 2, after `upgradeJakarta`.
3. **New request for the PDFs:** before each review or reading question, please add a short "study first" note (PDF section, file, concept). Without it, I could not answer the questions directly.

### Still to do

- [ ] Part 4: deploy `task-web`, check `STARTED com.acme.task.web`, add the widget, test Add / validation / Mark Done / Post-Redirect-Get
- [ ] Part 5: Experiments G (XSS), H (namespace), I (another site's task), J (javax strings)
- [ ] Part 3.6: the 5 reading questions, in English
- [ ] Review question 3: why "Add Task" is a link (render URL) but "Mark Done" a POST form (action URL)
- [ ] Part 1.2: prediction and result of the `servicecomponent` query
- [ ] Part 1.3: Lesson 3 answers (Part 6, Experiment F step 3 `javax.sql`, Experiment D check question)
- [ ] Part 6 (Lesson 4): three questions in English
