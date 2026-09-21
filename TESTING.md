# Test Matrix

Manual test cases run against the application before publishing, covering authentication, the passenger booking flow, admin fraud review, and all three fraud detection rules. All rows below reflect the expected and observed behaviour at time of testing.

## Login Screen

| Test | Steps | Expected |
| --- | --- | --- |
| Empty fields | Click Login with nothing entered | Error: "Please enter both username and password" |
| Wrong password | Valid username, incorrect password | Error: "Invalid username or password" |
| Passenger login | Valid passenger credentials | Opens Passenger Dashboard |
| Admin login | Valid admin credentials | Opens Admin Dashboard |
| Register link | Click "Create New Account" | Opens Registration screen |

## Registration Screen

| Test | Steps | Expected |
| --- | --- | --- |
| Missing fields | Leave Full Name blank, click Create | Error: "Please fill in all required fields" |
| Short password | Enter a password under 6 characters | Error: "Password must be at least 6 characters" |
| Password mismatch | Enter different passwords in the two fields | Error: "Passwords do not match" |
| Duplicate username | Register with an existing username | Error: "Username already taken" |
| Valid registration | Fill all fields correctly | Success dialog -> redirects to Login |

## Passenger Dashboard

| Test | Steps | Expected |
| --- | --- | --- |
| Welcome message | Log in as a passenger | "Welcome back, [Name]!" |
| Card hover | Hover over any action card | Card highlights on hover |
| Book Ticket | Click Book Ticket card | Opens Book Ticket screen |
| My Bookings | Click My Bookings card | Opens booking history |
| Cancel Ticket | Click Cancel Ticket card | Opens cancellation screen |
| Logout | Click Logout card or button | Returns to Login screen |

## Book Ticket Screen

| Test | Steps | Expected |
| --- | --- | --- |
| Search all trains | Leave From/To blank, click Search | All trains listed |
| Search by route | Enter matching From/To stations | Matching trains shown |
| Search by date | Enter a specific departure date | Schedules for that date shown |
| No results | Search a route with no matches | "No trains found" message |
| Seat map | Click any train row | Seat map appears on right panel |
| Available seat | Click an available seat | Seat highlights as selected, price shown, Confirm button enables |
| Booked seat | Try clicking an already-booked seat | Not clickable |
| Confirm booking | Select seat, click Confirm Booking | "Booking confirmed! #ID" dialog |
| **R01 test** | Book the same schedule again within 1 hour | Blocked - "Duplicate booking detected" |
| **R02 test** | Book 4+ tickets within 10 minutes | Blocked - "Too many bookings in a short time" |

## My Bookings Screen

| Test | Steps | Expected |
| --- | --- | --- |
| View history | Click My Bookings | All bookings shown in table |
| Stat cards | Check top summary cards | Correct counts for Total / Confirmed / Cancelled / Flagged |
| Status colours | Check the Status column | Confirmed / cancelled / flagged render with distinct styling |
| Refresh | Click Refresh | Table reloads latest data |
| Book new | Click "+ Book New Ticket" | Navigates to Book Ticket |
| Back | Click "<- Dashboard" | Returns to Passenger Dashboard |

## Cancel Ticket Screen

| Test | Steps | Expected |
| --- | --- | --- |
| View active bookings | Click Cancel Ticket | Only confirmed bookings shown |
| Cancel a booking | Click Cancel on any row | Confirmation dialog appears |
| Confirm cancel | Click OK in dialog | Booking removed from list, success message |
| Dismiss cancel | Click Cancel in dialog | Nothing changes |
| **R03 test** | Cancel 3+ bookings within 24 hours | Warning - "Excessive cancellations," account flagged |

## Admin Dashboard

| Test | Steps | Expected |
| --- | --- | --- |
| Admin login | Valid admin credentials | Opens Admin Dashboard |
| Stat cards | Check summary cards | Correct counts for Bookings / Passengers / Open Alerts / Total Alerts |
| Recent alerts | View table | Unreviewed alerts shown with severity indicators |
| High severity | Check R01/R02 alerts | Rendered with high-severity styling |
| Medium severity | Check R03 alerts | Rendered with medium-severity styling |
| Mark reviewed | Click "Mark Reviewed" | Alert removed from the unreviewed queue |
| View All Alerts | Click "View All Alerts" | Opens full alerts screen |
| Sidebar navigation | Click "Fraud Alerts" in sidebar | Switches to alerts screen |
| Logout | Click Logout | Returns to Login |

## Admin - All Alerts Screen

| Test | Steps | Expected |
| --- | --- | --- |
| Filter by severity | Select a severity, click Apply Filter | Only matching alerts shown |
| Filter by status | Select Reviewed/Unreviewed, click Apply Filter | Only matching alerts shown |
| Show all | Select "All" for both filters | All alerts shown |
| Refresh | Click Refresh | Reloads data |
