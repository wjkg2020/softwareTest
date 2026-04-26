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