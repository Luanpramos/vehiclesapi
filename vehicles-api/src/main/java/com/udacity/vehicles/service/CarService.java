package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final MapsClient mapsClient;
    private final PriceClient priceClient;

    public CarService(CarRepository repository, MapsClient mapsClient, PriceClient priceClient) {
        this.repository = repository;
        this.mapsClient = mapsClient;
        this.priceClient = priceClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car getCarById(Long id) throws CarNotFoundException {

        Car car = repository.findById(id).orElseThrow(() -> new CarNotFoundException("Car not found with the id: "+id));

        /**
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */

        String price = priceClient.getPrice(id);
        car.setPrice(price);
        Location address = mapsClient.getAddress(car.getLocation());
        car.setLocation(address);

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car saveOrUpdate(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(updatedCar -> {
                        updatedCar.setDetails(car.getDetails());
                        updatedCar.setLocation(car.getLocation());
                        updatedCar.setCondition(car.getCondition());
                        updatedCar.setPrice(car.getPrice());
                        updatedCar.setModifiedAt(LocalDateTime.now());
                        return repository.save(updatedCar);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */

    public void delete(Long id) {

        Car car = repository.findById(id).orElseThrow(() -> new CarNotFoundException("Car not found with the id: "+id));
        repository.delete(car);
    }
}
