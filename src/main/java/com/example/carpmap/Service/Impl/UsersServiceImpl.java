package com.example.carpmap.Service.Impl;

import com.example.carpmap.Models.DTO.Users.ErrorRegister;
import com.example.carpmap.Models.DTO.Users.RegisterDTO;
import com.example.carpmap.Models.Entity.Country;
import com.example.carpmap.Models.Entity.User;
import com.example.carpmap.Models.Entity.UserRole;
import com.example.carpmap.Repository.CountryRepository;
import com.example.carpmap.Repository.UserRepository;
import com.example.carpmap.Repository.UserRoleRepository;
import com.example.carpmap.Service.UsersService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.carpmap.Cammon.UsersMessages.*;

@Service
public class UsersServiceImpl implements UsersService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final CountryRepository countryRepository;

    public UsersServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository,
                            ModelMapper modelMapper, PasswordEncoder passwordEncoder,
                            CountryRepository countryRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.countryRepository = countryRepository;
    }

    @Override
    public void addAdminIfNotExist() {
        if (userRepository.count() == 0) {
            User firstAdmnUser = new User();
            mapFirstAdmin(firstAdmnUser);

            userRepository.save(firstAdmnUser);
            System.out.printf(SUCCESSFUL_REGISTER_USER,
                    firstAdmnUser.getFirstName(), firstAdmnUser.getLastName(),
                    firstAdmnUser.getUsername(), firstAdmnUser.getEmail());
        }
    }

    @Override
    public List<ErrorRegister> registerNewUser(RegisterDTO registerDTO) {

        List<ErrorRegister> errors = new ArrayList<>();
        ErrorRegister errorRegister = new ErrorRegister();
        String firstNameToUpperCaseFirstLetter = registerDTO.getFirstName().substring(0, 1).toUpperCase()
                + registerDTO.getFirstName().substring(1);
        String lastNameToUpperCaseFirstLetter = registerDTO.getLastName().substring(0, 1).toUpperCase()
                + registerDTO.getLastName().substring(1);

        String username = registerDTO.getUsername();
        String email = registerDTO.getEmail();

        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            errorRegister.setError(PASSWORD_NOT_MATCH);
            errors.add(errorRegister);
        } else if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            errorRegister.setError(EMAIL_EXIST);
            errors.add(errorRegister);
        } else if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            errorRegister.setError(USERNAME_EXIST);
            errors.add(errorRegister);
        }

        if (!errors.isEmpty()) {
            System.out.printf(ERROR_REGISTER_USER, firstNameToUpperCaseFirstLetter, lastNameToUpperCaseFirstLetter,
                    username, email);
            for (ErrorRegister error : errors) {
                System.out.printf("%n %s ", error.getError());
            }
            return errors;
        }

        User userRegister = modelMapper.map(registerDTO, User.class);
        userRegister.setFirstName(firstNameToUpperCaseFirstLetter);
        userRegister.setLastName(lastNameToUpperCaseFirstLetter);
        userRegister.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        userRegister.setCreateOn(LocalDate.now());
        userRegister.setRoles(List.of(getAllUserRoles()
                .get(2)));
        System.out.printf(SUCCESSFUL_REGISTER_USER, firstNameToUpperCaseFirstLetter, lastNameToUpperCaseFirstLetter,
                username, email);
        userRepository.save(userRegister);

        return errors;
    }

    private void mapFirstAdmin(User firstAdmnUser) {
        Optional<Country> bg = countryRepository.findById(1L);
        bg.ifPresent(country -> firstAdmnUser.setCountry(country.getCountry()));
        firstAdmnUser.setFirstName("Nikolay");
        firstAdmnUser.setLastName("Ivanov");
        firstAdmnUser.setCreateOn(LocalDate.now());
        firstAdmnUser.setEmail("admin@admin");
        firstAdmnUser.
                setPassword("734909fa01c605fa23051a67f16de8f522a2ef6969341effb1cbc4cac8d03860837749d5521cca0f654ceca0ea6f4aa2");
        firstAdmnUser.setUsername("admin");
        firstAdmnUser.setTeam("Carpoholics");
        firstAdmnUser.setCity("Gorna Oryahovitsa");
        firstAdmnUser.setPhoneNumber("0899524251");
        firstAdmnUser.setFacebook("https://www.facebook.com/Psyhopath/");
        firstAdmnUser.setInstagram("https://www.instagram.com/maddeniggaz/");
        firstAdmnUser.setJob("CNC Operator");
        List<UserRole> all = getAllUserRoles();
        firstAdmnUser.setRoles(all);
    }

    private List<UserRole> getAllUserRoles() {
        List<UserRole> allRoles = userRoleRepository.findAll();
        return allRoles;
    }


}
