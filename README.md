MindScale: Application Specification & Rules
Core Mandate: 1:1 Replication

This document outlines the explicit rules for creating the native Android (Kotlin) version of the MindScale application. The prime directive is to achieve a 1:1 functional and visual replication of the provided JavaScript reference implementation.

No Feature Creep: Do not add, alter, or remove any features, UI elements, or calculations described herein.

Identical Logic: The logic for data handling, state management, and statistical calculations must be ported exactly.

Visual Fidelity: The UI must match the colors, fonts, spacing, and layout of the reference app.

Visual & UI Specifications

2.1. Color Palette
The application uses a specific and consistent color palette.

Name:         Gold
Hex Code:     #ffce00
Usage:        Primary accent, buttons, charts

Name:         Turbo
Hex Code:     #ffe000
Usage:        Highlight part of the gold gradient

Name:         Gold Gradient
Hex Code:     #ffe000 to #ffce00
Usage:        Gradient always starting from the top left and ending bottom right

Name:         Dark Gray Gradient
Hex Code:     #555555 to #222222
Usage:        Gradient always starting from the top left and ending bottom right

Name:         Sleep/Wake
Hex Code:     #36437f
Usage:        Chart dots for sleep/wake entries

Name:         Intensity 1-10
Hex Code:     Varies
Usage:        See 2.2. Number Pad & Gradients

2.2. Number Pad & Gradients
Gold Gradient: A linear gradient from Turbo (#ffe000) to Gold (#ffce00) is used for primary buttons and selected states.

Gray Gradient: A linear gradient from Light Gray (#555555) to Dark Gray (#222222) is used for the "Sleep" button.

Intensity Gradients: Each number button (1-10) has a unique gradient. The color is derived from a base hex code, which is then lightened by 35% for the start of the gradient.

Base Colors: 1: #E8E8E8, 2: #D8D8D8, 3: #C8C8C8, 4: #B8B8B8, 5: #A8A8A8, 6: #989898, 7: #888888, 8: #787878, 9: #686868, 10: #000000.

Button 0: Uses the primary Gold Gradient.

Text Color: Text on buttons 0-6 is black. Text on buttons 7-10 is white.

2.3. Typography & Icons
Font: The application must use the Montserrat font family.

Regular (400)

Semibold (600)

Extrabold (800)

Icons: The app uses a specific set of icons. The Kotlin implementation must use equivalent vector drawables.

Moon, Sun, TrendingUp, Edit2, Trash2, ArrowLeft, ChevronLeft, ChevronRight, Calendar, Check, X, ChevronDown, FileText, AlertTriangle, Info, Bold, Italic, Underline, Strikethrough, RotateCcw, RotateCw.

Data Model & Persistence

3.1. Entry Data Structure
Each entry must conform to the following structure:

id: String - A unique identifier (e.g., UUID).

intensity: Int? - A nullable integer from 0 to 10.

timestamp: Long - A Unix timestamp in milliseconds.

type: String - Must be one of "depression", "sleep", or "wake".

notes: String? - An optional, nullable string for user notes, supporting basic HTML for formatting.

3.2. Persistence Layer
The Android app must use the Room Persistence Library for local data storage.

Data must be saved and loaded in a way that preserves the integrity of the data model.

Core Application Views & Functionality

4.1. Main Log View
This is the primary screen of the application.

Header: Displays "MINDSCALE" in uppercase, extrabold Montserrat font.

Number Pad:

A grid of buttons from 0 to 10 for logging entries. 5 rows. Top row is just 0, bottom row is just 10.

Tap Interaction: A single tap on a number creates a new "depression" entry with the corresponding intensity at the current time.

Long-Press Interaction: A press held for 500ms must trigger a modal allowing the user to select a custom date and time for the entry (as well as an “X” button to cancel the entry altogether).

Sleep/Wake Toggle:

Two buttons: "Sleep" and "Wake".

Toggling "Sleep" sets the sleepMode to sleep. The subsequent number pad entry will be of type sleep.

Toggling "Wake" sets the sleepMode to wake. The subsequent number pad entry will be of type wake.

When either are toggled, a gold-gradient border appears around the toggled button as well as another around the numpad.

Tapping an already active mode deactivates it, returning to the default "depression" entry type.

Recent Entries:

A list displaying the last 50 entries in reverse chronological order.

Each item shows the intensity badge, type icon (sleep/wake - if applicable), timestamp, and action icons (Edit Note, Edit Entry, Delete).

Import/Export:

Export: Must generate a JSON file containing all user entries. File must be titled “MindScale Export” + MM/DD/YYYY + HH:MM

Import: Must be able to parse a valid JSON file.

Conflict Resolution: If an imported entry has the same timestamp (down to the minute) as an existing entry, a modal must appear, forcing the user to choose to "Overwrite Existing" or "Keep Existing" (or an “X” button to cancel the import).

4.2. Trends View
This view visualizes user data over time.

Main Chart:

Line Interpolation: The line graph is not a simple plot of points. It follows the following critical rules:

24-Hour Carry (MAX_CARRY_MS): An entry's intensity value lasts for a maximum of 24 hours. If the next entry is more than 24 hours later, a break (a gap) must be rendered in the line (starting at the 24 hour point).

30-Minute Slope (MAX_SLOPE_MS): When the intensity changes between two entries, the line remains flat at the first entry's value until 30 minutes before the second entry, unless the two entries are at or less than 30 minutes apart (in which case the slope to connect the two points can begin immediately). The slope is linear.

Sleep / Wake: Whenever an entry is created with the “sleep” condition, the line must stop. The line must continue starting from the next entry (unless it is also a sleep entry). The dot for the entry point must be #36437f.

Chart Points: There need to be dots on the chart for ENTRIES ONLY. All dots must be gold (#ffce00) unless they have the sleep or wake condition.

Dot On-Hover: Whenever a dot is hovered over, there needs to be a tooltip with two rows. The first row shows the intensity rating (use the numpad icon), and the second row shows the date/time of the entry. If the entry has either a sleep or wake condition, put the corresponding icon (moon or sun) at the top right of the tooltip.

Dynamic Spacing: For any chart that displays greater than 14 days total, have the chart engine split the chart horizontally into 80 equal sections. Each 1/80th section must only ever have a single dot, representing the average of all entries within that range.

Statistical Summaries:

Time-Weighted Average: All averages (Hourly, Daily, Monthly) must be calculated using a time-weighted algorithm. A simple arithmetic mean is incorrect.

Sleep Trends: A sleep cycle is strictly defined as one sleep entry followed by one wake entry, with a duration of no more than 15 hours.

4.3. Calendar View
Month Grid: Displays a standard monthly calendar. Days with entries have colored dots within the box, with the color corresponding to the intensity of the entries on that day, up to 4 entries before an ellipse is added. (Must be able to scroll left and right through the months, as well as be able to manually select the exact month/year desired).

Entry List: Below the calendar is a scrollable list of all entries in reverse chronological order.

Interaction: Tapping a day in the calendar grid must automatically scroll the entry list to the last entry recorded (the most recent) on that selected day. If the calendar is scrolled, the entry list must also scroll to the last entry of the selected month.

4.4. Note Editor
A modal for editing entry notes.

Must support basic rich text: Bold, Italic, Underline, and ~Strikethrough~.

Must include Undo and Redo functionality.
