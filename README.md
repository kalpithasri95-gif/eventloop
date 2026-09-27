# EVENTLOOP – Smart Event Resource Reuse and Pre-Purchase Decision System

> **A Complete Real-World Java Desktop Application for Colleges, Universities, NGOs, and Corporate Event Teams.**
> Designed with Core Java, Java Swing, SQLite JDBC, and Comprehensive Object-Oriented Programming (OOP) Principles.

---

## 1. Project Background & Core Differentiator

During campus events, reusable resources (projectors, extension cables, banquet tables, chairs, standee frames, generic banners, delegate kits, audio equipment) are repeatedly purchased because organizers lack visibility into existing assets, their physical condition, verification status, and scheduling availability.

**EventLoop** is **not** a basic CRUD inventory system. Its core purpose is:

> **Pre-Purchase Decision Support:** Before any department initiates a new purchase request, EventLoop systematically evaluates whether an existing resource can be **reused, repaired, borrowed, rented, or repurposed**, backed by an economic decision matrix and potential cost avoidance calculations.

---

## 2. Project Folder Structure

```text
EventLoop/
│
├── compile.bat                  # One-click Windows build script
├── run.bat                      # One-click desktop application launcher (Swing + FlatLaf)
├── run_web.bat                  # One-click local web server launcher (Browser UI)
├── test.bat                     # One-click automated 18-point test suite
├── package.bat                  # Generates standalone EventLoop.jar
├── Dockerfile                   # Cloud container build for 24/7 hosting (Render/Railway)
├── render.yaml                  # 100% Free Cloud Deployment configuration
├── DEPLOYMENT_GUIDE.md          # Step-by-step guide to host online for free 24/7
├── eventloop.db                 # SQLite relational database (auto-created on first run)
├── web/                         # Modern Responsive Browser Web App (HTML/CSS/JS)
├── README.md                    # Comprehensive documentation and viva guide
│
├── lib/                         # External runtime libraries
│   ├── sqlite-jdbc-3.45.1.0.jar # SQLite JDBC Driver
│   ├── flatlaf-3.4.1.jar        # Modern Flat Look and Feel UI Engine
│   ├── slf4j-api-1.7.36.jar     # Logging facade
│   └── slf4j-simple-1.7.36.jar  # Simple logger
│
├── bin/                         # Compiled Java bytecode (.class files)
│
└── src/main/java/com/eventloop/
    │
    ├── interfaces/              # Mandatory Java Interfaces
    │   ├── Resource.java        # void displayDetails(); void inspect();
    │   ├── Reservable.java      # boolean reserve(); void release();
    │   ├── Repairable.java      # double calculateRepairCost(); void markUnderRepair();
    │   └── Reusable.java        # boolean canBeReused(); String getReuseRecommendation();
    │
    ├── model/                   # Data Models & POJOs
    │   ├── User.java            # System User (Admin / Organizer)
    │   ├── EventModel.java      # Event Lifecycle & Purchase Avoidance
    │   ├── RequirementModel.java# Event Resource Requirements
    │   ├── ReservationModel.java# Time-based Reservations & Deadlines
    │   ├── InspectionModel.java # Pre-Use & Post-Use Physical Inspections
    │   ├── HistoryModel.java    # Audit Trail Timeline Log
    │   ├── CostComparison.java  # Pre-Purchase Decision Matrix Model
    │   ├── CompatibilityMatch.java # 10-Point Inventory Matching Result
    │   │
    │   └── resources/           # OOP Inheritance Hierarchy
    │       ├── AbstractResource.java     # Base resource class implementing Resource
    │       ├── EquipmentResource.java   # Intermediate category
    │       ├── FurnitureResource.java   # Intermediate category
    │       ├── DecorationResource.java  # Intermediate category
    │       ├── StationeryResource.java  # Intermediate category
    │       ├── ElectricalResource.java  # Intermediate category
    │       ├── AudioVisualResource.java # Intermediate category
    │       ├── Projector.java           # Implements Resource, Reservable, Repairable, Reusable
    │       ├── ExtensionCable.java      # Implements Resource, Reservable, Repairable, Reusable
    │       ├── Table.java               # Implements Resource, Reservable, Reusable
    │       ├── Chair.java               # Implements Resource, Reservable, Reusable
    │       ├── Banner.java              # Implements Resource, Reusable
    │       ├── CustomPrintedBanner.java # Extends Banner -> Reusable (Repurpose Output)
    │       ├── StandeeFrame.java        # Implements Resource, Reservable, Reusable
    │       ├── NameBadge.java           # Implements Resource, Reusable
    │       ├── Speaker.java             # Implements Resource, Reservable, Repairable, Reusable
    │       ├── Microphone.java          # Implements Resource, Reservable, Repairable, Reusable
    │       ├── RegistrationKit.java     # Implements Resource, Reusable
    │       └── ResourceFactory.java     # Polymorphic Subtype Factory
    │
    ├── dao/                     # Data Access Object Layer (JDBC + SQLite)
    │   ├── DatabaseManager.java # Schema creation & Sample data seeder
    │   ├── UserDAO.java         # Authentication & User persistence
    │   ├── ResourceDAO.java     # Inventory persistence & Polymorphic loader
    │   ├── EventDAO.java        # Event & Requirement persistence
    │   ├── ReservationDAO.java  # Time-overlap conflict queries & bookings
    │   ├── InspectionDAO.java   # Pre & Post use inspection persistence
    │   └── HistoryDAO.java      # Immutable audit trail persistence
    │
    ├── service/                 # Business Logic Layer
    │   ├── AuthService.java     # User session management
    │   ├── ResourceService.java # Inventory operations & state transitions
    │   ├── EventService.java    # Mandatory event closure workflow gate
    │   ├── MatchingService.java # 10-point compatibility matching engine
    │   ├── DecisionSupportService.java # Reuse/Repair/Borrow/Rent/Buy economic matrix
    │   ├── ReservationService.java # Date/time overlap detection & checkouts
    │   ├── InspectionService.java  # Quality assurance & physical checklists
    │   └── ReportService.java   # 10 business reports & CSV exporter
    │
    ├── exception/               # Custom Domain Exceptions
    │   ├── DuplicateResourceException.java
    │   ├── ResourceNotAvailableException.java
    │   ├── ReservationConflictException.java
    │   ├── VerificationRequiredException.java
    │   ├── InvalidEventDateException.java
    │   ├── EventClosureException.java
    │   └── ValidationException.java
    │
    ├── util/                    # Utility Classes
    │   ├── DateUtil.java        # Time-interval overlap & expiry calculations
    │   └── ValidationUtil.java  # Input integrity & formatting guards
    │
    ├── ui/                      # Desktop Presentation Layer (Java Swing)
    │   ├── MainFrame.java       # Primary application window & navigation
    │   ├── LoginDialog.java     # Masked sign-in with Quick Demo roles
    │   ├── RegisterDialog.java  # User registration dialog
    │   │
    │   ├── components/          # Reusable UI Styling Elements
    │   │   ├── ModernUIUtils.java # Theme palette, typography, badges, cards
    │   │   └── StatCard.java    # KPI dashboard metric component
    │   │
    │   └── panels/              # Functional Modules
    │       ├── DashboardPanel.java          # 12 KPI summary cards & live feeds
    │       ├── ResourceCatalogPanel.java    # Inventory search, filter & actions
    │       ├── AddEditResourceDialog.java   # Resource creation form
    │       ├── ResourceDetailsDialog.java   # Details & interface badge display
    │       ├── VerificationPanel.java       # Verification audit & expiry gate
    │       ├── EventManagementPanel.java    # Event scheduling & status tracking
    │       ├── CreateEventDialog.java       # Event creation form
    │       ├── AddRequirementDialog.java   # Requirements specification form
    │       ├── EventDetailsDialog.java      # Event profile & allocation manifest
    │       ├── RequirementMatchingPanel.java# 10-point matching results
    │       ├── CostComparisonDialog.java    # Pre-Purchase Decision Matrix UI
    │       ├── ReservationPanel.java        # Bookings, checkouts & returns
    │       ├── InspectionDialog.java        # Pre-Use & Post-Use checklists
    │       ├── EventClosureDialog.java      # Mandatory Event Closure Guard
    │       ├── ReportsPanel.java            # 10 Reports & CSV Exporter
    │       ├── ResourceHistoryDialog.java   # Resource audit trail dialog
    │       └── OOPPolymorphismDemoPanel.java# Interactive OOP viva demo lab
    │
    └── main/                    # Main Entry & Verification
        ├── EventLoopApp.java        # Desktop Application Entry Point
        └── EventLoopTestSuite.java  # Automated 18-Point Test Suite
```

---

## 3. Java Interfaces & OOP Architecture

Java Interfaces are the structural backbone of EventLoop, modeling distinct hardware and lifecycle capabilities:

```java
// Base interface implemented by all physical resources
public interface Resource {
    void displayDetails();
    void inspect();
}

// Implemented by resources that can be scheduled for time-based events
public interface Reservable {
    boolean reserve();
    void release();
}

// Implemented by resources that can undergo technical repair or servicing
public interface Repairable {
    double calculateRepairCost();
    void markUnderRepair();
}

// Implemented by resources that support reuse or repurposing evaluation
public interface Reusable {
    boolean canBeReused();
    String getReuseRecommendation();
}
```

### Class Hierarchy & Interface Implementation Matrix

| Class Name | Superclass | Implemented Interfaces | Distinct Capability / Behavior |
| :--- | :--- | :--- | :--- |
| **`Projector`** | `AudioVisualResource` | `Resource`, `Reservable`, `Repairable`, `Reusable` | Optical lens check, lamp repair cost calculation, high-value asset reuse |
| **`ExtensionCable`** | `ElectricalResource` | `Resource`, `Reservable`, `Repairable`, `Reusable` | Strict electrical safety rule: blocked from reservation if condition ≤ 2 |
| **`Table`** | `FurnitureResource` | `Resource`, `Reservable`, `Reusable` | Direct reuse or cover recommendation, locking pin inspection |
| **`Chair`** | `FurnitureResource` | `Resource`, `Reservable`, `Reusable` | Cushion & frame condition tracking, bulk stacking reservation |
| **`Banner`** | `DecorationResource` | `Resource`, `Reusable` | Direct reuse for generic institutional welcome branding |
| **`CustomPrintedBanner`**| `Banner` | `Resource`, `Reusable` | **Polymorphic Override**: `getReuseRecommendation()` returns **REPURPOSE** recommendation (reverse side backdrop/craft) |
| **`StandeeFrame`** | `EquipmentResource` | `Resource`, `Reservable`, `Reusable` | Hardware frame reused while only graphic flex is swapped |
| **`NameBadge`** | `StationeryResource` | `Resource`, `Reusable` | Direct reuse of clear PVC holders and lanyards; print inserts only |
| **`Speaker`** | `AudioVisualResource` | `Resource`, `Reservable`, `Repairable`, `Reusable` | Amplifier & woofer testing, live auditorium acoustic clearance |
| **`Microphone`** | `AudioVisualResource` | `Resource`, `Reservable`, `Repairable`, `Reusable` | UHF wireless frequency pairing and battery contact check |
| **`RegistrationKit`** | `StationeryResource` | `Resource`, `Reusable` | Leatherette folder reuse or repurpose for committee volunteers |

### Key OOP Principles Demonstrated

1. **Multiple Interface Implementation**:
   `Projector` and `ExtensionCable` implement `Resource`, `Reservable`, `Repairable`, and `Reusable` simultaneously.
2. **Interface Reference & Runtime Polymorphism**:
   ```java
   Resource res = new Projector(); // Interface reference to subtype
   res.displayDetails();           // Dynamic method dispatch

   if (res instanceof Repairable) {
       Repairable rep = (Repairable) res;
       double cost = rep.calculateRepairCost();
   }
   ```
3. **Inheritance & Specialization**:
   `CustomPrintedBanner` inherits from `Banner`, which inherits from `DecorationResource`, which inherits from `AbstractResource`. It overrides `getReuseRecommendation()` to enforce **REPURPOSE** logic.
4. **Encapsulation & Validation**:
   All state is protected or private with validated mutators ensuring positive quantities, valid condition ratings (1-5), and non-negative costs.

---

## 4. Database Schema (SQLite)

EventLoop automatically creates the following relational schema in `eventloop.db` upon initial launch:

1. **`users`**: User credentials, role (`ADMIN`, `ORGANIZER`), full name, email, department.
2. **`resources`**: Unique resource ID, name, category, description, total & available quantity, condition rating (1-5), status (`AVAILABLE`, `RESERVED`, `IN_USE`, `UNDER_INSPECTION`, `UNDER_REPAIR`, `MISSING`, `RETIRED`, `REPURPOSE_REQUIRED`), verification status (`VERIFIED`, `VERIFICATION_REQUIRED`, `EXPIRED`), storage coordinates (building, room, rack, shelf, box), costs, specs.
3. **`events`**: Event ID, name, organizer, department, event type, date, start/end time, venue, participants, event status (`DRAFT`, `REQUIREMENTS_ADDED`, `RESOURCES_RESERVED`, `IN_PROGRESS`, `RETURN_PENDING`, `INSPECTION_PENDING`, `COMPLETED`), potential purchase avoided.
4. **`event_requirements`**: Event ID, required category, name, quantity, minimum condition, specs, datetime, indoor/outdoor requirement, status.
5. **`reservations`**: Reservation ID, event ID, resource ID, quantity, start datetime, end datetime, return deadline, status (`REQUESTED`, `APPROVED`, `ACTIVE`, `COMPLETED`, `OVERDUE`, `REJECTED`).
6. **`resource_inspection`**: Reservation ID, resource ID, event ID, inspection type (`PRE_USE`, `POST_USE`), inspector, condition rating, checked/returned/missing quantities, damage flags, outcome status, findings.
7. **`resource_history`**: Audit trail of every action (`CREATED`, `VERIFIED`, `RESERVED`, `CHECKED_OUT`, `RETURNED`, `INSPECTED`, `REPAIRED`, `RETIRED`, `REPURPOSED`).
8. **`repairs`**: Tracking maintenance liabilities, descriptions, estimates, and technician details.

---

## 5. End-to-End Event Resource Lifecycle

```text
  [ Create Event ]
         │
         ▼
  [ Add Requirements ] (Category, specs, quantity, min condition)
         │
         ▼
  [ Smart Matching Engine ] (10-point compatibility check)
         │
         ▼
  [ Pre-Purchase Decision Matrix ] (Reuse vs Repair vs Borrow vs Rent vs Buy)
         │
         ▼
  [ Verification Gate ] (Blocks expired/unverified resources)
         │
         ▼
  [ Reservation System ] (Detects date/time overlap conflicts)
         │
         ▼
  [ Pre-Use Inspection & Checkout ] (Handover checklist, stock deducted)
         │
         ▼
  [ Conduct Event ] (Status: IN_PROGRESS)
         │
         ▼
  [ Resource Return ] (Status: RETURN_PENDING)
         │
         ▼
  [ Post-Use Inspection ] (Damage check, missing parts, cleaning, condition update)
         │
         ▼
  [ Event Closure Guard ] (STRICT CHECK: blocks if items unreturned or uninspected)
         │
         ▼
  [ Event COMPLETED ] (Purchase budget protected)
```

---

## 6. Pre-Purchase Decision Matrix & Cost Avoidance Formula

### Decision Matrix Columns
* Existing Resource Availability & Condition (1-5)
* Direct Reuse Cost (₹)
* Estimated Repair Cost (₹)
* Inter-Department Borrowing Cost (₹)
* Commercial Rental Cost (₹)
* New Purchase Outlay (₹)
* **Potential Purchase Avoided (₹)**
* **Estimated Budget Savings (₹)**
* **Intelligent Recommendation & Reasoning**

### Formula
$$\text{Potential Purchase Avoided} = \text{Required Quantity} \times \text{New Purchase Unit Cost}$$

### Example Case
* Event requires: 1 High-Lumen Projector (New purchase cost: ₹45,000)
* Campus inventory has 1 verified projector in condition 4/5.
* **Direct Reuse Cost**: ₹0
* **Potential Purchase Avoided**: $1 \times ₹45,000 = \mathbf{₹45,000}$
* **Recommendation**:
  > *"Existing college inventory is verified and in good condition (4/5). Direct reuse is 100% recommended. It saves ₹45,000.00 with zero purchase expense!"*

---

## 7. How to Compile & Run

### Prerequisites
* Java Development Kit (JDK 11, 17, or 21)
* Windows Command Prompt or PowerShell

### Method 1: One-Click Batch Scripts (Recommended)
1. Double-click `compile.bat` to compile all source files into the `bin\` folder.
2. Double-click `run.bat` to launch the Desktop GUI application.
3. Double-click `test.bat` to execute the automated 18-point test suite.

### Method 2: Command Line (PowerShell / Command Prompt)
```powershell
# Compile
javac -cp "lib/*" -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)

# Launch Desktop GUI Application
java -cp "bin;lib/*" com.eventloop.main.EventLoopApp

# Run 18-Point Automated Test Suite
java -cp "bin;lib/*" com.eventloop.main.EventLoopTestSuite
```

---

## 8. Demo User Credentials

The application comes pre-populated with realistic campus event data and demo user accounts:

| Role | Username | Password | Full Name | Department / Entity |
| :--- | :--- | :--- | :--- | :--- |
| **Admin / Store Manager** | `admin` | `admin123` | Prof. Rajesh Sharma | Central Store & Purchase Division |
| **Event Organizer** | `organizer`| `org123` | Priya Raman | IEEE Student Branch |
| **Cultural Organizer** | `cultural` | `cult123` | Karthik Sundar | Fine Arts & Cultural Council |

*(The Sign-In screen also includes 1-click **Quick Demo Login** buttons for instant viva presentation without typing!)*

---

## 9. Automated Test Suite (18 Scenarios)

The test suite in `EventLoopTestSuite.java` exercises the entire business logic and exception handling:

1. **Add a valid resource**: Verifies resource insertion and location coordinates.
2. **Reject duplicate resource ID**: Asserts `DuplicateResourceException` when duplicate ID is used.
3. **Search an available resource**: Validates multi-parameter keyword and category search.
4. **Block an unverified resource**: Asserts `VerificationRequiredException` blocks booking unverified or expired assets.
5. **Block a retired resource**: Asserts `ResourceNotAvailableException` blocks booking retired assets.
6. **Detect overlapping reservations**: Asserts `ReservationConflictException` detects overlapping time intervals.
7. **Allow reservation for available quantity**: Verifies booking succeeds for unallocated stock.
8. **Reject reservation when quantity is insufficient**: Asserts `ResourceNotAvailableException` prevents overbooking.
9. **Mark a resource under repair**: Verifies state transition to `UNDER_REPAIR`.
10. **Prevent reservation of a resource under repair**: Confirms booking rejection while in repair.
11. **Return all resources successfully**: Verifies quantity reconciliation and transition to `COMPLETED`.
12. **Block event closure when resources are missing or awaiting inspection**: Asserts `EventClosureException` prevents premature event completion.
13. **Complete event closure after inspection**: Confirms event transitions to `COMPLETED` when all conditions are satisfied.
14. **Calculate potential purchase avoided correctly**: Validates mathematical accuracy of cost avoidance formula.
15. **Display repair versus rent versus buy comparison**: Verifies economic matrix and smart recommendation engine.
16. **Update resource history after actions**: Confirms audit log entries with timestamps and actors.
17. **Check that interface methods work through polymorphism**: Confirms dynamic dispatch across `Resource`, `Reservable`, `Repairable`, `Reusable`.
18. **Verify data persistence across restarts**: Confirms SQLite database integrity and state retention.

---

## 10. Viva Questions & Answers

### Q1: What makes EventLoop different from a standard inventory management system?
**A:** A standard inventory system only tracks what is in stock (CRUD). EventLoop is a **Pre-Purchase Decision Support System**. Before purchasing any new resource for an event, EventLoop checks category, specifications, physical condition, and reservation conflicts, providing a comparison between **Reuse, Repair, Borrow, Rent, and Buy**, calculating **Potential Purchase Avoided**, and enforcing a strict **Event Closure Guard**.

### Q2: Why are Java Interfaces necessary here instead of just concrete classes?
**A:** Different resources have distinct, non-overlapping capabilities that cannot be captured in a single inheritance tree:
* A `Projector` is `Reservable`, `Repairable`, and `Reusable`.
* A `Table` is `Reservable` and `Reusable`, but not electronically `Repairable`.
* A `CustomPrintedBanner` is `Reusable` (in a repurposed capacity), but cannot be reserved for another event with a different name.
Java Interfaces allow polymorphic handling of any resource by its capability (`Reservable`, `Repairable`, `Reusable`) regardless of its concrete class.

### Q3: How does EventLoop detect reservation conflicts?
**A:** Overlap detection uses standard interval algebra:
Two bookings $[Start_A, End_A]$ and $[Start_B, End_B]$ conflict if and only if:
$$Start_A < End_B \quad \text{AND} \quad End_A > Start_B$$
The system queries active bookings overlapping the requested date/time window and verifies whether remaining inventory satisfies the requested quantity. If not, a `ReservationConflictException` is thrown.

### Q4: What is the Verification Expiry system?
**A:** Campus resources undergo wear and tear. Every resource has a `lastVerifiedDate` and `nextVerificationDate`. If the current date exceeds `nextVerificationDate`, the resource status becomes `EXPIRED`. The system triggers a `VerificationRequiredException`, preventing reservation until a Store Manager inspects the physical item and certifies verification.

### Q5: What is the Event Closure Guard?
**A:** The Event Closure Guard prevents organizers from marking an event `COMPLETED` if any resource remains checked out, missing without documentation, overdue, or awaiting Post-Use Inspection. This ensures complete accountability and eliminates misplaced college property.

---

## 11. Limitations & Future Enhancements

### Current Scope & Design Rationale
* Uses local SQLite database for maximum reliability, speed, and zero external network configuration, ideal for standalone college and campus intranet use.
* Desktop GUI built using Java Swing with custom flat styling, high-contrast badges, and responsive tables.

### Future Enhancements
1. **QR Code Scanning**: Print asset QR stickers for instant physical checkout/return scanning via mobile camera.
2. **Automated Email/SMS Notifications**: Send automated reminders to organizers 2 hours before the return deadline.
3. **Inter-Campus Fleet Logistics**: Multi-campus resource borrowing with GPS van routing.
4. **IoT Asset Tracking**: BLE beacon tags inside high-value audio-visual equipment to trigger proximity alerts when removed from the auditorium.
#   e v e n t l o o p  
 