package com.example.demoe.Controller.Address;

import com.example.demoe.Dto.Address.AddressDto;
import com.example.demoe.Dto.Address.ListAddressDto;
import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.User;
import com.example.demoe.Repository.AddressRepo;
import com.example.demoe.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AddressController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AddressRepo addressRepo;

    @GetMapping("/getAddressDefault")
    public ResponseEntity<AddressDto> getAddressDefault(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        if(user1.isPresent()){
            User user2 = user1.get();
            List<Address> address = addressRepo.findListAddressByUser(user2.getId());
            for (Address address1 : address) {
                if(address1.getIsDefault()!=null&&address1.getIsDefault()==true){
                    return ResponseEntity.ok(new AddressDto("Success",address1));
                }
            }
        }
        else{
            return ResponseEntity.ok(new AddressDto("Not found user",null));
        }
        return ResponseEntity.ok(new AddressDto("Success",null));
    }

    @GetMapping("/getAllAddress")
    public ResponseEntity<ListAddressDto> getAllAddress(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        if(user1.isPresent()){
            User user2 = user1.get();
            List<Address> address = addressRepo.findListAddressByUser(user2.getId());

            return ResponseEntity.ok(new ListAddressDto("Success",address));
        }
        else{
            return ResponseEntity.ok(new ListAddressDto("Not found user",null));
        }
    }

    @PostMapping("/addAddress")
    public ResponseEntity<AddressDto> addAddress(@RequestBody Address address){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        if(user1.isPresent()){
            User user2 = user1.get();
            addressRepo.save(address);
            user2.addAddress(address);
            userRepo.save(user2);
            return ResponseEntity.ok(new AddressDto("Success",address));
        }
        else{
            return ResponseEntity.ok(new AddressDto("Not found user",null));
        }
    }

    @PostMapping("/setAddressDefault/{id}")
    public ResponseEntity<AddressDto> setAddressDefault(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        if(user1.isPresent()){
            User user2 = user1.get();
            List<Address> addressList = addressRepo.findListAddressByUser(user2.getId());
            Address address = addressRepo.findById(id).get();
            for (Address address1 : addressList) {
                if(address1.getId()==id){
                    address1.setIsDefault(true);
                    addressRepo.save(address1);
                }
                else if(address1.getIsDefault()!=null&&address1.getIsDefault()==true){
                    address1.setIsDefault(false);
                    addressRepo.save(address1);
                }

            }
            return ResponseEntity.ok(new AddressDto("Success",address));
        }
        else{
            return ResponseEntity.ok(new AddressDto("Not found user",null));
        }
    }
    @PutMapping("/updateAddress")
    public ResponseEntity<AddressDto> updateAddress(@RequestBody Address address){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        if(user1.isPresent()){
            User user2 = user1.get();
            Address address1=addressRepo.findById(address.getId()).get();
            if(address1!=null) {
                address1.setAddressType(address.getAddressType());
                address1.setProvince(address.getProvince());
                address1.setDistrict(address.getDistrict());
                address1.setIsDefault(address.getIsDefault());
                address1.setPhone(address.getPhone());
                address1.setWard(address.getWard());
                address1.setFullName(address.getFullName());
                address1.setNameAddress(address.getNameAddress());
                addressRepo.save(address1);}
            else{

                return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(new AddressDto("Not found address",null));
            }
            return ResponseEntity.ok(new AddressDto("Success",address));
        }
        else{
            return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(new AddressDto("Not found user",null));
        }
    }
}
