Wie ich das zum Laufen bringe?
1. ActiveMQ starten
2. Das Projekt als Maven konfigurieren und pom.xml mit Dependencies vorhanden haben
3. Die Dateien in folgender Reihenfolge ausführen lassen:
- CamelMain: Zentrale Schnittstelle, die alle Anfragen bearbeiten wird
- BillingSystem: Bevor die Bestellungen kommen, müssen wir schon gucken, ob genug Geld gibt oder?
- CallCenterOrderSystem: Die Bestellungen werden dann in die Dateien geschrieben und von dem schon laufenden CamelMain gelesen und weitergeleitet
