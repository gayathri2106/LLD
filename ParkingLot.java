import java.util.Map;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
enum VehicleType{
    CAR, BIKE, TRUCK
};
class AbstractEntity{
    private Long id;
    Long getId(){
        return id;
    }
};
class Vehicle extends AbstractEntity {
    private final VehicleType vehicleType;
    private final String vehicleNo;

    public Vehicle(VehicleType vehicleType, String vehicleNo){
        this.vehicleType = vehicleType;
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }
    public VehicleType getVehicleType(){
        return vehicleType;
    }
};
class Ticket extends AbstractEntity{
    private Slot slot;
    private Vehicle vehicle;
    private LocalDateTime exitTime;
    private LocalDateTime parkedTime;

    public Ticket(Slot slot, Vehicle vehicle){
        this.slot = slot;
        this.vehicle = vehicle;
        this.parkedTime = LocalDateTime.now();
    }

    public void close(){
        this.exitTime = LocalDateTime.now();
    }

    Duration getDuration(){
        return Duration.between(parkedTime, exitTime);
    }
    Vehicle getVehicle(){
        return vehicle;
    }
    Slot getSlot(){
        return slot;
    }
};
class Slot extends AbstractEntity{
    private Boolean isOccupied;
    private VehicleType vehicleType;

    public Slot(VehicleType vehicleType){
        this.vehicleType = vehicleType;
        this.isOccupied = false;
    }

    void park(){
        this.isOccupied = true;
    }

    void unPark(){
        this.isOccupied = false;
    }

    VehicleType getVehicleType(){
        return this.vehicleType;
    }

    Boolean isAvailable(){
        return !this.isOccupied;
    }
};
class Level extends AbstractEntity{
    private Map<Long,Slot> slots;

    public Level(Map<Long,Slot> slots){
        this.slots = slots;
    }

    public Map<Long,Slot> getSlots(){
        return slots;
    }
};
class ParkingLot extends AbstractEntity{
    private Map<Long,Level> floors;
    private Map<String,Ticket> activeTickets;
    private ParkingStrategy parkingStrategy;
    private FeeStrategy feeStrategy;

    Map<Long,Level> getFloors(){
        return floors;
    }

    void setStrategy(ParkingStrategy parkingStrategy){
        this.parkingStrategy = parkingStrategy;
    }

    void setFeeStrategy(FeeStrategy feeStrategy){
        this.feeStrategy = feeStrategy;
    }

    public synchronized Ticket  parkVechile(Vehicle vehicle){
        Optional<Slot> slot = this.parkingStrategy.findSlot(floors,vehicle);
        if(slot.isEmpty()){
            return null;
        }
        Ticket ticket = new Ticket(slot.get(), vehicle);
        slot.get().park();
        activeTickets.put(vehicle.getVehicleNo(), ticket);
        return ticket;
    }

    public synchronized Double unParkVechile(Ticket ticket){
        ticket.getSlot().unPark();
        ticket.close();
        activeTickets.remove(ticket.getVehicle().getVehicleNo());
        return feeStrategy.getFee(ticket);
    }
};
interface ParkingStrategy{
    public Optional<Slot> findSlot(Map<Long,Level> floors, Vehicle vechile);
}
class NearestStrategy implements ParkingStrategy{
    @Override
    public Optional<Slot> findSlot(Map<Long,Level> floors, Vehicle vechile){
        for(Level level: floors.values()){
            for(Slot slot: level.getSlots().values()){
                if(slot.isAvailable()  && slot.getVehicleType() ==  vechile.getVehicleType()){
                    return Optional.of(slot);
                }
            }
        }
        return Optional.empty();
    }
}
interface FeeStrategy{
    public Double getFee(Ticket ticket);
}
class HourlyBasisStrategy implements FeeStrategy{
    Map<VehicleType,Double> priceMap = new HashMap<>();

    public HourlyBasisStrategy() {
        priceMap.put(VehicleType.CAR, 50.0);
        priceMap.put(VehicleType.BIKE, 20.0);
        priceMap.put(VehicleType.TRUCK, 100.0);
    }
    
    @Override
    public Double getFee(Ticket ticket){
       Duration duration = ticket.getDuration();
        // Get the total difference in hours
        long hours = Math.max(1, (duration.toMinutes() + 59) / 60);
        return priceMap.get(ticket.getVehicle().getVehicleType())*hours;
    }
}