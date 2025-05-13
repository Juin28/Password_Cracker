# Password_Cracker
A Java-based UNIX password cracker that uses dictionary attacks with layered mangling rules to recover encrypted passwords from /etc/passwd-style files.

# 🔐 PasswordCrack.java: Version Comparison

## 🔍 High-Level Differences

| Aspect | **First Version** | **Second Version** |
|--------|-------------------|--------------------|
| **Mangling strategy** | Applies **all manglings at once** per word (flat list) | Applies manglings **layer by layer** (breadth-first: original → one mangling → two manglings) |
| **Phase structure** | Three distinct phases (GCOS, basic mangling, extra mangling) | Two phases (GCOS + breadth-first layered mangling) |
| **Mangling function design** | Two separate methods: `generateManglings()` and `generateExtraManglings()` | Single function `applySingleManglings()` used repeatedly for layering |
| **Password cracking logic** | `tryCrackAllManglings()` and `tryCrackExtraManglings()` for each user | `crackWordAtLevel()` applied to a list of guesses per mangling layer |
| **Efficiency** | May test the same transformations multiple times (no reuse) | Avoids deep unnecessary mangling until previous layers fail (more efficient) |
| **Scalability** | Flat mangling list grows quickly | More structured and scalable with breadth-first expansion |

---

## 🧠 Conceptual Explanation

### ✅ First Version: **Phased with Predefined Manglings**

- **Phase 1:** Try GCOS words with `generateManglings()`
- **Phase 2:** Try dictionary words with the same manglings
- **Phase 3:** Try dictionary words with **extra manglings**

Each word goes through a fixed list of transformations, such as:
- `admin`, `ADMIN`, `Admin`, `123admin`, `admin123`, `adminadmin`, etc.

**Pros:**
- Simple to implement and understand
- Covers a wide range of mangles

**Cons:**
- Inefficient due to large number of guesses
- Doesn’t prioritize simpler variants first

---

### ✅ Second Version: **Breadth-First, Layered Manglings**

- **Phase 1:** Try GCOS words **without** mangling
- **Phase 2:** For each dictionary word:
  - Try original word (layer 0)
  - Apply one mangling rule (layer 1)
  - Apply a second mangling rule on each layer 1 result (layer 2)

For example, with "admin":
- **Layer 0:** `admin`
- **Layer 1:** `Admin`, `admin123`, `123admin`, etc.
- **Layer 2:** `123Admin123`, `ADMINADMIN`, etc.

**Pros:**
- Efficient and realistic (simpler mangles first)
- Prioritized cracking (breadth-first)

**Cons:**
- More complex code
- May miss deep mangling combinations unless layers are extended

---

## 🛠 Code Structural Differences

| Feature | **First Version** | **Second Version** |
|--------|-------------------|--------------------|
| Mangling functions | `generateManglings()` and `generateExtraManglings()` | `applySingleManglings()` used recursively |
| Cracking method | Inline via `tryCrackAllManglings()` and `tryCrackExtraManglings()` | Modular via `crackWordAtLevel()` |
| Modularity | Less modular | More modular and clean separation of concerns |

---

## ✅ When to Use Which?

| Goal | Recommended Version |
|------|----------------------|
| Simplicity and broad mangling coverage | **First Version** |
| Efficiency and realistic prioritization | **Second Version** |
| Project requires breadth-first search logic | **Second Version** |
| Quick brute-force tool | **First Version** |

---

Let me know if you want a hybrid version that combines their strengths!
