# Courses Management

Desktop app for managing training courses, built with JavaFX and SQLite for the OOP course project.

## Team

| Member | GitHub | Part |
|---|---|---|
| عبدالرحمن البعداني | [@USERNAME1](https://github.com/USERNAME1) | Auth & Base: users table, DBconnection, ParentUsers, Authentication, Welcome / Login / Sign up screens |
| مهند حاجب | [@USERNAME2](https://github.com/USERNAME2) | Admin & Courses: courses table, Admin, Course, Admin Dashboard, Add Course, View Courses |
| أحمد الحمادي | [@USERNAME3](https://github.com/USERNAME3) | Student & Enrollment: enrollments and reviews tables, Student, Enrollment, Review, Browse Courses, My Courses, Review window |
| رامز مدحت | [@mrramez](https://github.com/mrramez) | Trainer & Attendance: attendance table and JOIN queries, Trainer, Attendance, Trainer Dashboard, My Students, Attendance |

## Report

The full project report is in [docs/Report.pdf](docs/Report.pdf).

## Requirements

- JDK 25
- IntelliJ IDEA

JavaFX 25, SQLite JDBC and jBCrypt are downloaded by Maven from `pom.xml`.

## How to run

1. In IntelliJ choose File > Open and select this folder.
2. Wait until Maven finishes loading. If IntelliJ asks for the SDK, choose JDK 25.
3. Run the `Launcher` configuration.

From a terminal you can also run `mvnw.cmd javafx:run` (Windows) or `./mvnw javafx:run`.

The database file `app.db` is created automatically in the project folder on the first run.

## First use

1. Sign up a trainer account and an admin account.
2. Log in as the admin and add courses. Every course needs a trainer.
3. Sign up a student account to browse courses, enroll, pay and write reviews.
4. Log in as the trainer to see the students and take attendance.

On the welcome screen, "Continue as guest" opens the course list without an account.

## Project structure

```
src/main/java/org/example/finaloop
    model        ParentUsers, Student, Trainer, Admin, Course, Enrollment, Review, Attendance
    database     DBconnection, DataBaseStart
    service      Authentication, Session, ImageStore
    ui           Navigator, Ui, CourseCover
    controller   one controller for each screen
src/main/resources/org/example/finaloop
    view         FXML screens
    styles       theme.css
```
