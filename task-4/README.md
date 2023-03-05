# How to run it?

1. Create following folders:
- billing_validated_orders
- call_center_orders
- inventory_validated_orders
- validated_orders

2. Start ActiveMQ 
3. Configure as a  Maven project konfigurieren having pom.xml and all dependencies
4. Run files in following order:

- BillingSystem: before order comes, we should check, whether we have enough money.
- InventorySysten
- CallCenterOrderSystem
- CamelMain
- WebOrderSystem
- ResultSystem
