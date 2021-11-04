package com.casestudyecommerce.restController;

import com.casestudyecommerce.cart.CartDetail;
import com.casestudyecommerce.cart.ICartService;
import com.casestudyecommerce.security.model.UserPrinciple;
import com.casestudyecommerce.user.users.IUserService;
import com.casestudyecommerce.user.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ICartService cartService;

    @Autowired
    private IUserService userService;

    @GetMapping
    public ResponseEntity<Page<CartDetail>> findAll(Pageable pageable) {
        return new ResponseEntity<>(cartService.findAll(pageable), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CartDetail> addToCart(@RequestBody CartDetail cartDetail, Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
            User user = userService.findByUsername(userPrinciple.getUsername());
            cartDetail.setUser(user);
            return new ResponseEntity<>(cartService.save(cartDetail), HttpStatus.CREATED);
        }
    }

    @PutMapping("/{action}/{id}")
    public ResponseEntity<CartDetail> changeQuantity(@PathVariable("action") String action, @PathVariable("id") Long id, Authentication authentication) {
        Optional<CartDetail> optionalCartDetail = cartService.findById(id);
        if (!optionalCartDetail.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            CartDetail cartDetail = optionalCartDetail.get();
            int quantity = cartDetail.getQuantity();
            switch (action) {
                case "plus":
                case "+":
                    cartDetail.setQuantity(quantity + 1);
                    break;
                case "minus":
                case "-":
                    cartDetail.setQuantity(quantity - 1);
                    if (cartDetail.getQuantity() == 0) {
                        cartService.deleteById(id);
                        return new ResponseEntity<>(HttpStatus.OK);
                    }
                    break;
                default:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(cartService.save(cartDetail), HttpStatus.OK);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(Authentication authentication, @PathVariable Long id) {
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            Optional<CartDetail> optionalCartDetail = cartService.findById(id);
            if (!optionalCartDetail.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
                if (!(userPrinciple.getId() == optionalCartDetail.get().getUser().getId())) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                } else {
                    cartService.deleteById(id);
                    return new ResponseEntity<>("Success", HttpStatus.OK);
                }
            }
        }
    }



}
