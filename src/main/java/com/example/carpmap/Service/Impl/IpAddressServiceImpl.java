package com.example.carpmap.Service.Impl;

import com.example.carpmap.Models.Entity.IpAddress;
import com.example.carpmap.Models.Entity.User;
import com.example.carpmap.Repository.IpAddressRepository;
import com.example.carpmap.Repository.UserRepository;
import com.example.carpmap.Service.IpAddressService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IpAddressServiceImpl implements IpAddressService {

    private final IpAddressRepository ipAddressRepository;
    private final UserRepository userRepository;

    public IpAddressServiceImpl(IpAddressRepository ipAddressRepository, UserRepository userRepository) {
        this.ipAddressRepository = ipAddressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getIp() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof WebAuthenticationDetails details) {

            return details.getRemoteAddress();
        }
        return "Cant detect ip";
    }

    @Override
    public Long findAllVisits() {
        Long countAllVisitors = ipAddressRepository.sumAllCounts();
        return countAllVisitors;
    }

    @Override
    public void checkIpAddressLogin(String username, String ipAddress) {
        Optional<IpAddress> findExistingIpAddress = ipAddressRepository.findByAddress(ipAddress);

        if (findExistingIpAddress.isEmpty()) {
            IpAddress newAddress = new IpAddress();
            Optional<User> findUser = userRepository.findByUsername(username);
            newAddress.setAddress(ipAddress);
            newAddress.setTimeToAdd(LocalDateTime.now());
            newAddress.setCountVisits(1L);
            findUser.ifPresent(newAddress::setUser);
//            newAddress.setUser(findUser.get());
            ipAddressRepository.save(newAddress);
        } else if (findExistingIpAddress.get().getUser() == null) {
            Optional<User> findUser = userRepository.findByUsername(username);
            findUser.ifPresent(user -> findExistingIpAddress.get().setUser(user));
            addView(findExistingIpAddress);
            ipAddressRepository.save(findExistingIpAddress.get());
        } else {
            addView(findExistingIpAddress);
            ipAddressRepository.save(findExistingIpAddress.get());
        }
    }

    private static void addView(Optional<IpAddress> findExistingIpAddress) {
        if (findExistingIpAddress.isPresent()) {
            findExistingIpAddress.get().setCountVisits(findExistingIpAddress.get().getCountVisits() + 1L);
            findExistingIpAddress.get().setLastSeen(LocalDateTime.now());
        }
    }

    @Override
    public void getIpVisitor(String ipAddress) {
        Optional<IpAddress> byAddress = ipAddressRepository.findByAddress(ipAddress);
        if (byAddress.isEmpty()) {
            IpAddress addNewIpVisitor = new IpAddress();
            addNewIpVisitor.setAddress(ipAddress);
            addNewIpVisitor.setTimeToAdd(LocalDateTime.now());
            addNewIpVisitor.setCountVisits(1L);
            ipAddressRepository.save(addNewIpVisitor);
        } else {
            IpAddress newIpAdd = byAddress.get();
            newIpAdd.setCountVisits(newIpAdd.getCountVisits() + 1L);
            newIpAdd.setLastSeen(LocalDateTime.now());
            ipAddressRepository.save(newIpAdd);
        }
    }
}
