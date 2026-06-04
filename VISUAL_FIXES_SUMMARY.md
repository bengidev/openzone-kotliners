# Visual Design Fixes - Android Onboarding

> Implementation notes for a past onboarding UI pass. Ongoing documentation: [docs/README.md](docs/README.md), [CONTEXT-MAP.md](CONTEXT-MAP.md) (Onboarding context), [AGENTS.md](AGENTS.md) (onboarding layering).

## Analysis Summary

Compared iOS reference screenshots (`right-impls/`) with Android implementation (`wrong-impls/`) and identified the following visual discrepancies:

### Issues Identified

#### 1. Encrypted Pairing Page (Page 1)
**Problem**: "ROTATE KEY" button was positioned inside the center lock node and styled with a black background.
**iOS Reference**: Light blue pill button positioned BELOW the device row (LOCAL + OPENZONE).
**Fix Applied**:
- Removed button from inside `CenterLockNode`
- Positioned as separate element below device row in main `Column`
- Updated styling to match iOS: light blue background with accent border
- Increased device node sizes for better visual balance
- Simplified center lock node (removed container box, just the lock icon)

#### 2. Idea Studio Page (Page 2)
**Problem**: Skeleton loading lines were crammed inside the text input box, making it look cluttered. Missing "DEMO" section header.
**iOS Reference**: Clean text input area with skeleton lines positioned BELOW as separate elements, "DEMO" label above text box.
**Fix Applied**:
- Added "DEMO" section header between prompt chips and text area
- Moved skeleton lines outside the text input box
- Restructured layout: chips → DEMO label → text input → skeleton lines
- Reduced vertical spacing for tighter rhythm

#### 3. Prompt Queue Page (Page 3)
**Problem**: Queue cards lacked visual hierarchy and section separation.
**iOS Reference**: Clear "RUNNING" and "NEXT" section headers above each card with color-coded status.
**Fix Applied**:
- Added section headers (RUNNING/NEXT) above each queue card
- Color-coded headers match status colors (blue for RUNNING, brown for NEXT)
- Increased vertical spacing between card groups
- Refactored to pass `statusColor` to `QueueCard` to avoid duplication

#### 4. Reasoning Control Page (Page 4)
**Problem**: Element order didn't match iOS. Missing percentage label below slider.
**iOS Reference**: Ring+label → Chart → Preset buttons → Slider → Percentage label
**Fix Applied**:
- Reordered elements to match iOS flow
- Moved compute budget chart ABOVE preset buttons
- Added "62%" percentage label below slider
- Increased card height to accommodate additional element
- Improved chart height for better visual balance

#### 5. Workspace Ready Page (Page 5)
**Problem**: Content was vertically centered, tags used bordered pills instead of plain text.
**iOS Reference**: Top-weighted layout, plain text tags with dot separators (·), CONTINUE button above tags.
**Fix Applied**:
- Changed from `Arrangement.Center` to `Arrangement.Top` with top padding
- Replaced bordered pill tags with plain text: "AGENTS · PROMPTS · MODELS · REVIEW · SHIP"
- Added dark pill "CONTINUE →" button between body text and tags
- Reduced headline font size (38sp instead of 44sp) for better balance
- Removed unused `FeatureTag` composable

### Card Height Adjustments
Increased card heights across all pages to accommodate visual content:
- Encrypted Pairing: 244dp → 260dp
- Idea Studio: 244dp → 252dp
- Prompt Queue: 260dp → 268dp
- Reasoning Control: 252dp → 280dp (for percentage label)

## Files Modified

Paths are relative to `app/src/main/java/io/github/bengidev/openzone/onboarding/presenter/`.

1. `visuals/EncryptedPairingVisualView.kt` - Button positioning and styling
2. `visuals/IdeaStudioVisualView.kt` - Layout structure and DEMO label
3. `visuals/PromptQueueVisualView.kt` - Section headers and color refactoring
4. `visuals/ReasoningControlVisualView.kt` - Element reordering and percentage label
5. `visuals/WorkspaceReadyVisualView.kt` - Layout alignment, tags, and CONTINUE button
6. `FeaturePageView.kt` - Card height adjustments

## Design Principles Applied

- **iOS Fidelity**: Matched iOS reference designs pixel-for-pixel where possible
- **Visual Hierarchy**: Clear section separation and color-coded status indicators
- **Consistency**: Maintained consistent spacing, typography, and color tokens
- **Simplicity**: Removed unnecessary visual complexity (bordered pills, cluttered layouts)
- **Top-Weighted Layouts**: Content flows naturally from top, not centered vertically

## Testing Recommendations

1. Verify all 5 onboarding pages render correctly in both light and dark themes
2. Test animations and transitions between pages
3. Confirm interactive elements (Rotate Key, preset buttons, slider) respond correctly
4. Check that card heights accommodate content without clipping
5. Validate that plain text tags in Workspace Ready page wrap correctly on smaller screens
