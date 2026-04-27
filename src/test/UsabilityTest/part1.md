# Usability Evaluation Report: JPass Password Manager (Phase 1)

This report details the usability testing and heuristic evaluation for the first four core interfaces of JPass, as part of the MSCS coursework requirements.

---

## 1. Main Window (MainFrame)

**User Test Case: Efficient Navigation & Sorting**
* **Scenario**: The user has dozens of entries and needs to verify the last modification date for a specific account.
* **Task**: Locate the entry modified on **"2026-04-25"** without using the search bar.
* **Expected Behavior**: The user should intuitively click the **"Modified"** column header to sort entries chronologically, bringing the target entry to the top of the list.

**Heuristic Evaluation: H2 — Match between System and Real World**
* **Observation**: The interface utilizes standard desktop metaphors, such as folders for file management and a magnifying glass for search functions.
* **Finding**: **Moderate Risk**. Several toolbar icons (e.g., Folder with "+" vs. Folder with arrow) are visually similar and lack immediate text labels.
* **Recommendation**: Implement descriptive tooltips or optional text labels for toolbar icons to clarify functions and reduce cognitive load for new users.

---

## 2. Master Password Dialog

**User Test Case: Secure Database Initialization**
* **Scenario**: A user is setting up a new vault for the first time to store sensitive credentials.
* **Task**: Create a new database and set the master password to `JHU_MSCS_2026`.
* **Expected Behavior**: The user inputs the password into both the **"Password"** and **"Repeat"** fields and clicks **"Ok"** to confirm.

**Heuristic Evaluation: H5 — Error Prevention**
* **Observation**: The dialog explicitly requires the user to repeat the password in a secondary field.
* **Finding**: **High Effectiveness**. This design choice prevents catastrophic errors where a user might be permanently locked out due to a simple typo during the initial vault setup.
* **Recommendation**: Add a **"Caps Lock"** detection warning to the password field to prevent accidental case-sensitive errors that are difficult for users to troubleshoot.

---

## 3. Entry Dialog (Add New Entry)

**User Test Case: Integrated Account Creation**
* **Scenario**: The user needs to add a new GitHub account entry with a unique, secure password.
* **Task**: Fill in the Title, URL, and Username, then generate a secure password using the integrated tool.
* **Expected Behavior**: The user fills in the text fields and clicks the **"Generate"** button, which triggers the secondary generator window.

**Heuristic Evaluation: H7 — Flexibility and Efficiency of Use**
* **Observation**: High-frequency actions including **"Show"**, **"Generate"**, and **"Copy"** are placed directly beneath the password fields.
* **Finding**: **Excellent Design**. These "accelerators" cater to power users by allowing them to manage credentials efficiently without navigating away from the main entry form.
* **Recommendation**: Ensure the **"Notes"** text area supports auto-wrapping and scrollbars to better accommodate long-form data like recovery codes or security questions.

---

## 4. Password Generator Dialog

**User Test Case: Meeting Custom Security Constraints**
* **Scenario**: A website requires a 16-character password consisting only of numbers and symbols.
* **Task**: Configure the generator to produce a 16-character password using only **"Numbers"** and **"Custom symbols"**.
* **Expected Behavior**: The user adjusts the **"Password length"** spinner to 16, unchecks the letter options, and clicks **"Generate"**.

**Heuristic Evaluation: H1 — Visibility of System Status**
* **Observation**: The generated password is displayed in a dedicated preview field for immediate review.
* **Finding**: **Minor Issue**. While the status of the generation is visible, there is no real-time **"Strength Meter"** to provide feedback on the entropy of the user's custom settings.
* **Recommendation**: Integrate a color-coded strength indicator (Red to Green) to help users understand the security implications of their chosen constraints.
  
---

## 5. Searching Window

**User Test Case: Keyboard Navigation**
* **Scenario**: A user is actively working in another application and switches back to the password manager to quickly retrieve a specific account using their keyboard.
* **Task**: Use a keyboard shortcut to focus the search field, type a keyword, and locate the desired credential without using the mouse.
* **Expected Behavior**: The user presses a standard searching shortcut, the cursor immediately focuses within the "Find" input field, and the list filters as the user types.

**Heuristic Evaluation: Consistency and Standards**
* **Observation**: The user attempts to initiate a search using standard keyboard shortcuts but cannot find any visual cue indicating if shortcuts are supported.
* **Finding**: **Minor Issue**. The "Find" label and input box lack a visual hint regarding keyboard shortcuts. It did not follow macOS system convention.
* **Recommendation**: Add "Find" to the tool bar text or the input box that explicitly states "Find (Control+F)" to improve discoverability and align with platform standards.

---

## 6. Configuration Dialog

**User Test Case: Exporting Database with System Conventions**
* **Scenario**: A user wants to export their current JPass password database to an XML file for backup purposes.
* **Task**: Open the export configuration dialog, navigate to the desired folder, enter a file name, and finalize the export.
* **Expected Behavior**: The dialog interface matches macOS native UI guidelines, and the UI language is consistently localized (either entirely in English or entirely in the system's local language).

**Heuristic Evaluation: Consistency and Standards**
* **Observation**: The Export dialog exhibits mixed localization (e.g., "Export" for the window title and primary button, but "查找(I):" and "取消" for other controls). Additionally, labels include Windows-style accelerator indicators like `(N)` and `(T)`, which are non-standard and often non-functional on macOS.
* **Finding**: **Major Issue**. This is a classic cross-platform GUI framework artifact. The mixed languages confuse the user, and the presence of Windows accelerators on a Mac breaks platform conventions, making the application feel unpolished and untrustworthy for handling sensitive security data.
* **Recommendation**: 
    1. Unify the language resource bundles to ensure consistent localization based on the user's OS locale setting. 
    2. Strip Windows-specific accelerator text (like `(&N)`) when the application detects it is running on a macOS environment, or better yet, utilize the OS's native File Chooser API instead of a custom UI dialogue.
  
---

## 7. Searching Window

**User Test Case: Keyboard Navigation**
* **Scenario**: A user is actively working in another application and switches back to the password manager to quickly retrieve a specific account using their keyboard.
* **Task**: Use a keyboard shortcut to focus the search field, type a keyword, and locate the desired credential without using the mouse.
* **Expected Behavior**: The user presses a standard searching shortcut, the cursor immediately focuses within the "Find" input field, and the list filters as the user types.

**Heuristic Evaluation: Consistency and Standards**
* **Observation**: The user attempts to initiate a search using standard keyboard shortcuts but cannot find any visual cue indicating if shortcuts are supported.
* **Finding**: **Minor Issue**. The "Find" label and input box lack a visual hint regarding keyboard shortcuts. It did not follow macOS system convention.
* **Recommendation**: Add "Find" to the tool bar text or the input box that explicitly states "Find (Control+F)" to improve discoverability and align with platform standards.

---
