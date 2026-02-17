@echo off
echo ========================================
echo Running tests with Allure report
echo ========================================
echo.
echo Tests will be launched, then it will open
echo browser with an interactive report.
echo.
echo There is no need to clean anything, the program will clean itself upon completion of testing.
echo To stop the server, press Ctrl+C
echo ========================================
echo.

mvn test allure:serve
mvn clean

pause
