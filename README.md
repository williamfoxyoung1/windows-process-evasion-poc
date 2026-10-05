# Task Manager Evasion POC

A Java proof-of-concept demonstrating **monitor-aware process behavior on Windows**.

When Windows Task Manager (`Taskmgr.exe`) opens, the managed demonstration process terminates. When Task Manager closes, the process starts again.

The demonstration uses **Notepad (`notepad.exe`)** as a harmless test process.

> **For educational and authorized security research only.**

## Demo
https://github.com/user-attachments/assets/14b2869a-b02a-4c3d-b3fc-7d60998c0af6

The demo shows:

**Task Manager closed → Notepad runs**  
**Task Manager opened → Notepad terminates**  
**Task Manager closed → Notepad relaunches**

## Requirements

- Windows 10/11
- JDK 17+
- Command Prompt

## Build & Run

Compile:

```cmd
javac taskmgr_evasion_poc\taskmgr_evasion_poc.java
```

Create the runnable JAR:

```cmd
jar --create --file taskmgr_evasion_poc.jar --main-class taskmgr_evasion_poc.taskmgr_evasion_poc taskmgr_evasion_poc\taskmgr_evasion_poc.class
```

Run:

```cmd
java -jar taskmgr_evasion_poc.jar
```

Open Task Manager with **Ctrl + Shift + Esc** to test the POC.

## Change the Managed Process

The demonstration uses Notepad:

```java
private static final String MANAGED_EXE = "notepad.exe";
```

Change `MANAGED_EXE` to the benign executable you want to use in your lab:

```java
private static final String MANAGED_EXE = "BenignPOC.exe";
```

For a full Windows path:

```java
private static final String MANAGED_EXE =
    "C:\\Lab\\BenignPOC.exe";
```

Recompile and rebuild the JAR after making changes.

## Troubleshooting

### Correct Java Version

Run:

```cmd
java -version
```

This indicates a compatible Java version:

```text
openjdk version "17.0.1" 2021-10-19
```

Java **17 or later** is recommended.

### Incorrect Java Version

This is Java 8 and is too old for the Java 17 build:

```cmd
java -version
```

```text
java version "1.8.0_503"
```

Running the JAR with Java 8 may result in:

```text
java.lang.UnsupportedClassVersionError
```

If multiple Java versions are installed, check which installation Windows is using:

```cmd
where java
where javac
```

Then confirm:

```cmd
java -version
javac -version
```

## Concepts Demonstrated

- Windows process monitoring
- Java `ProcessBuilder`
- PID-based process control
- Process lifecycle management
- State-transition detection
- Runnable JAR creation
- Behavior relevant to malware analysis and defense-evasion research

## Disclaimer

This is a **benign educational proof-of-concept**. Notepad is used as a harmless and easily observable demonstration process.

Use only on systems you own or have explicit authorization to test. This project is not intended to disable or interfere with security, monitoring, or administrative software.
