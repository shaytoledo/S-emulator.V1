# S-Emulator

An interpreter and visual debugger for the **S-language** — a formal computational model used in theoretical computer science. Programs are written in XML and executed step-by-step through a JavaFX GUI. The project has grown across three exercises:

- **EX1** — Basic S-instructions (increment, decrement, jump-if-not-zero, noop)
- **EX2** — Synthetic (composite) instructions that expand into basic ones at configurable depth levels
- **EX3** — Client/server split: a multi-user HTTP server hosts programs and a JavaFX client connects to it

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 21 or later |
| IntelliJ IDEA | 2023.1 or later (Community or Ultimate) |
| JavaFX | 22.0.2 (bundled in `resources/javaFX/`) |

No Maven or Gradle. The project uses IntelliJ's **native build system** — all dependencies are pre-bundled under `resources/`.

---

## Project Structure

```
S-Emulator1/
├── engine/src/          # Core execution engine (no GUI dependency)
│   ├── core/engine/     # EngineImpl — public API
│   ├── core/program/    # ProgramImpl, Function, VariableAndLabelMenger
│   ├── logic/execution/ # ProgramExecutorImpl, ExecutionContextImpl
│   ├── logic/instruction/
│   │   ├── basic/       # 4 basic instructions (INC, DEC, JNZ, NOOP)
│   │   └── synthetic/   # 9 composite instructions
│   └── adapter/         # JAXB XML loader + translator
│
├── gui/src/application/ # JavaFX frontend
│   ├── AppController.java          # EX1/EX2 standalone entry point
│   ├── main/  top/  left/  right/  # Standalone GUI controllers
│   └── client/                     # EX3 client
│       ├── ClientLauncher.java     # EX3 client entry point
│       ├── login/                  # Login screen
│       ├── dashboard/              # Main dashboard (users, programs, history)
│       └── execution/              # Program execution / debug screen
│
├── server/src/application/ # EX3 HTTP server (port 8080)
│   ├── ServerMain.java
│   ├── handlers/           # HTTP route handlers
│   ├── service/            # UserRegistry, ProgramRegistry, ExecutionService
│   └── model/              # UserInfo, ProgramEntry, DebugSession, etc.
│
├── test/                # Sample XML programs (composition, divide, math, …)
├── resources/           # Bundled JARs: JavaFX 22.0.2, JAXB, Gson
└── .idea/               # IntelliJ project files (run configs, artifacts)
```

---

## Opening the Project

1. Launch IntelliJ IDEA.
2. **File → Open** → select the `S-Emulator1` folder.
3. IntelliJ will detect the four modules automatically (`S-Emulator1`, `engine`, `gui`, `server`).
4. If the SDK is not set: **File → Project Structure → Project SDK** → choose Java 21.
5. Build once: **Build → Build Project** (`Ctrl+F9`).

---

## Running — EX1 / EX2 Standalone GUI

This mode runs the emulator locally with no server. One user, one program at a time.

**Entry point:** `application.AppController` in the `gui` module.

**In IntelliJ:**

1. Open **Run → Edit Configurations**.
2. If no configuration exists, add **Application**, set:
   - Main class: `application.AppController`
   - Module classpath: `gui`
3. Click **Run**.

**What you see:**

- **Top toolbar** — Load an XML file, set the expansion level (0 = synthetic, N = expand N levels deep), enter input values (x1, x2, …), then press **Run** or **Debug**.
- **Left panel** — Instruction list, variable names, label names.
- **Right panel** — Output variable `y`, variable state table, step counter.

**Loading a test program:** click **Load File** and pick any `.xml` from the `test/` folder or write your own (see [Writing Programs](#writing-programs)).

---

## Running — EX3 Client/Server Mode

EX3 splits the emulator into a shared HTTP server and multiple JavaFX clients. Users log in, upload programs, and run/debug them from the dashboard.

### 1. Start the server

**Entry point:** `application.ServerMain` in the `server` module.

In IntelliJ, use the pre-built run configuration **RunServer**, or add one manually:

- Main class: `application.ServerMain`
- Module classpath: `server`

The server starts on **port 8080** and prints the available endpoints to the console.

### 2. Start a client

**Entry point:** `application.client.ClientLauncher` in the `gui` module.

Use the run configuration **RunClientEx3**:

- Main class: `application.client.ClientLauncher`
- Module classpath: `gui`

Multiple clients can run simultaneously — use **Run → RunClientEx3** multiple times or click **New Window** inside the dashboard.

### 3. Workflow

1. **Login** — enter any username (first time creates the account; the same username cannot be logged in twice simultaneously).
2. **Dashboard** — shows all users, uploaded programs, functions, and your run history.
3. **Upload** — click **Upload Program**, pick an XML file; the program (and its functions) become available to all users.
4. **Run** — select a program or function from the table, click **Run Program** / **Run Function**, set inputs and level, then **Run** or **Debug**.
5. **Credits** — each instruction cycle costs 1 credit. New users start with 1 000 credits. Click **Add Credits** to top up.

---

## Writing Programs

Programs are XML files. A minimal example (`test/successor.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<S-Program name="Successor"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:noNamespaceSchemaLocation="S-Emulator-v2.xsd">
  <S-Instructions>
    <S-Instruction type="synthetic" name="ASSIGNMENT">
      <S-Variable>y</S-Variable>
      <S-Instruction-Arguments>
        <S-Instruction-Argument name="assignedVariable" value="x1"/>
      </S-Instruction-Arguments>
    </S-Instruction>
    <S-Instruction type="basic" name="INCREASE">
      <S-Variable>y</S-Variable>
    </S-Instruction>
  </S-Instructions>
</S-Program>
```

### Variable naming convention

| Prefix | Role |
|--------|------|
| `x1, x2, …` | Input variables (set from the GUI) |
| `y` | Output variable (result shown in the GUI) |
| `z1, z2, …` | Work/temporary variables (auto-initialised to 0) |

### Basic instruction types (`type="basic"`)

| `name` | Effect |
|--------|--------|
| `INCREASE` | `var++` |
| `DECREASE` | `var--` (floors at 0) |
| `JUMP_NOT_ZERO` | if `var ≠ 0` goto label |
| `NOOP` | no operation |

### Synthetic instruction types (`type="synthetic"`)

| `name` | Meaning |
|--------|---------|
| `ZERO` | `var = 0` |
| `ASSIGNMENT` | `var = other` |
| `CONSTANT_ASSIGNMENT` | `var = k` |
| `GO_TO` | unconditional goto label |
| `JUMP_ZERO` | if `var = 0` goto label |
| `JUMP_EQUAL_CONSTANT` | if `var = k` goto label |
| `JUMP_EQUAL_VARIABLE` | if `var = other` goto label |
| `JUMP_EQUAL_FUNCTION` | if `var = f(…)` goto label |
| `QUOTE` | call a function (side-effect: sets `y`) |

Programs can also define `<S-Functions>` blocks; those sub-programs are callable from synthetic instructions that take a function argument.

More examples are in the `test/` directory: `divide.xml`, `math.xml`, `composition.xml`, `predicates.xml`, `remainder.xml`, etc.

---

## Building JARs

Artifacts are pre-configured in IntelliJ (**Build → Build Artifacts**):

| Artifact | Output | Contents |
|----------|--------|----------|
| `EngineJAR` | `out/artifacts/EngineJAR/EngineJAR.jar` | engine module only |
| `GuiJAR` | `out/artifacts/GuiJAR/GuiJAR.jar` | gui + engine + JavaFX + JAXB |
| `ServerJAR` | `out/artifacts/ServerJAR/ServerJAR.jar` | server + engine + Gson + JAXB |
| `ClientJAR` | `out/artifacts/ClientJAR/ClientJAR.jar` | gui (client) + JavaFX + Gson |

---

## Architecture Overview

```
XML file
  └─► JaxbLoader (JAXB)
        └─► SProgram (generated DTO)
              └─► ProgramTranslator
                    └─► ProgramImpl  ◄──── VariableAndLabelMenger
                          └─► ProgramExecutorImpl
                                └─► ExecutionContextImpl
                                      └─► GUI controllers  (or HTTP handlers in EX3)
```

**Expansion levels:** Synthetic instructions implement `extend(level)`. Level 0 keeps them as-is; level N replaces each synthetic instruction with its basic-instruction expansion, recursively. The left panel in the GUI shows the expanded list; the level slider controls depth.

---

## EX3 Server REST API

The server exposes a plain HTTP JSON API (no authentication tokens — username is passed per request):

```
POST /api/login                         login (or re-login)
POST /api/logout                        logout
GET  /api/users                         list all logged-in users
GET  /api/users/{name}/history          run history for a user
POST /api/programs                      upload an XML program
GET  /api/programs                      list all programs
GET  /api/functions                     list all functions
POST /api/execute/run                   start a full run
GET  /api/execute/poll/{id}             poll run result
POST /api/execute/debug/start           start a debug session
POST /api/execute/debug/{id}/step       single step
POST /api/execute/debug/{id}/resume     resume to completion
POST /api/execute/debug/{id}/stop       stop debug session
GET  /api/execute/program-info          get instruction list for a program
POST /api/credits/topup                 add credits to a user
```

---

## Known Limitations

- The server holds all state in memory; restarting it clears all users, programs, and history.
- JavaFX must be on the module path; the bundled JARs under `resources/javaFX/` cover this when running from IntelliJ with the pre-configured library.
- The client hard-codes the server at `localhost:8080`. To connect to a remote server, change `SERVER_HOST` in `LoginController.java`.
