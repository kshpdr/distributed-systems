Wie ich das zum Laufen bringe?

0. folgende Ordner im data directory erstellen:
- billing_validated_orders
- call_center_orders
- inventory_validated_orders
- validated_orders

1. ActiveMQ starten
2. Das Projekt als Maven konfigurieren und pom.xml mit Dependencies vorhanden haben
3. Die Dateien in folgender Reihenfolge ausführen lassen:


- BillingSystem: Bevor die Bestellungen kommen, müssen wir schon gucken, ob genug Geld gibt oder?
- InventorySysten
- CallCenterOrderSystem
- CamelMain
- WebOrderSystem
- ResultSystem

