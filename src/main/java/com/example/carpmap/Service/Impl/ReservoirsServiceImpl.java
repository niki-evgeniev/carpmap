package com.example.carpmap.Service.Impl;

import com.example.carpmap.Models.DTO.Reservoirs.*;
import com.example.carpmap.Models.Entity.Country;
import com.example.carpmap.Models.Entity.Fish;
import com.example.carpmap.Models.Entity.Reservoir;
import com.example.carpmap.Models.Entity.User;
import com.example.carpmap.Models.Enums.FishType;
import com.example.carpmap.Repository.CountryRepository;
import com.example.carpmap.Repository.FishRepository;
import com.example.carpmap.Repository.ReservoirRepository;
import com.example.carpmap.Repository.UserRepository;
import com.example.carpmap.Service.ReservoirsService;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.carpmap.Cammon.ErrorMessages.COUNTRY_NOT_FOUND;
import static com.example.carpmap.Cammon.ErrorMessages.USER_WHO_ADD_RESERVOIRS_NOT_FOUND;
import static com.example.carpmap.Cammon.SuccessfulMessages.*;

@Service
public class ReservoirsServiceImpl implements ReservoirsService {

    private final ModelMapper modelMapper;
    private final ReservoirRepository reservoirRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final FishRepository fishRepository;

    public ReservoirsServiceImpl(ModelMapper modelMapper, ReservoirRepository reservoirRepository,
                                 CountryRepository countryRepository, UserRepository userRepository,
                                 FishRepository fishRepository) {
        this.modelMapper = modelMapper;
        this.reservoirRepository = reservoirRepository;
        this.countryRepository = countryRepository;
        this.userRepository = userRepository;
        this.fishRepository = fishRepository;
    }


    @Override
    public boolean addReservoirs(ReservoirsAddDTO reservoirsAddDTO) {

        Reservoir addNewReservoirs = modelMapper.map(reservoirsAddDTO, Reservoir.class);


        Optional<Country> country = countryRepository.findByCountry(reservoirsAddDTO.getCountry());
        if (country.isPresent()) {
            addNewReservoirs.setCountry(country.get());
            Optional<User> findUser = userRepository.findById(1L);

            if (findUser.isPresent()) {
                List<Fish> fish = new ArrayList<>();
                for (String fishName : reservoirsAddDTO.getFishName()) {
                    Optional<Fish> findFishName = fishRepository.findByFishName(fishName);
                    findFishName.ifPresent(fish::add);
                    System.out.printf(SUCCESSFUL_ADD_FISH_TYPE_TO_RESERVOIR,
                            fishName, reservoirsAddDTO.getName());
                }

                addNewReservoirs.setFish(fish);
                addNewReservoirs.setUser(findUser.get());
                reservoirRepository.save(addNewReservoirs);
                System.out.printf(SUCCESSFUL_ADD_RESERVOIR,
                        reservoirsAddDTO.getName(), reservoirsAddDTO.getCountry());
                return true;
            } else {
                System.out.printf(USER_WHO_ADD_RESERVOIRS_NOT_FOUND,
                        reservoirsAddDTO.getName());
            }
        }
        System.out.printf(COUNTRY_NOT_FOUND, reservoirsAddDTO.getCountry());
        return false;
    }

    @Override
    public boolean checkNameExisting(String name) {
        Optional<Reservoir> existName = reservoirRepository.findByName(name);
        return existName.isPresent();
    }

    @Override
    public Page<ReservoirAllDTO> getAllReservoirs(Pageable pageable) {
        Page<Reservoir> findAllReservoir = reservoirRepository.findAll(pageable);
        Page<ReservoirAllDTO> allReservoirs = findAllReservoir.map(reservoir -> {
            return modelMapper.map(reservoir, ReservoirAllDTO.class);
        });
        return allReservoirs;
    }

    @Override
    public ReservoirsDetailsDTO getDetails(Long id) {
        Optional<Reservoir> findReservoir = reservoirRepository.findById(id);
        ReservoirsDetailsDTO reservoirsDetailsDTO = modelMapper.map(findReservoir, ReservoirsDetailsDTO.class);
        List<FishNameDTO> fishNameDTOS = new ArrayList<>();
        for (Fish fish : findReservoir.get().getFish()) {
            FishNameDTO fish1 = new FishNameDTO();
            fish1.setFishName(fish.getFishName());
            fishNameDTOS.add(fish1);
        }
        reservoirsDetailsDTO.setFishNameDTO(fishNameDTOS);
        System.out.println();
        return reservoirsDetailsDTO;
    }
}
