/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import cart.ShoppingCart;
import cart.ShoppingCartItem;
import entity.Customer;
import entity.CustomerOrder;
import entity.OrderedProduct;
import entity.OrderedProductPK;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Vonk
 */
@Stateless
public class OrderManager {
    
    @PersistenceContext(unitName = "AffableBeanPU")
    private EntityManager em;

    public int placeOrder(String name, String email, String phone, String address, String cityRegion, String ccNumber, ShoppingCart cart) {
        Customer customer = addCustomer(name, email, phone, address, cityRegion, ccNumber);
        CustomerOrder order = addOrder(customer, cart);
        addOrderedItems(order, cart);
        return order.getId();
    }

    private Customer addCustomer(String name, String email, String phone, String address, String cityRegion, String ccNumber) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setCityRegion(cityRegion);
        customer.setCcNumber(ccNumber);
        
        // to wright to the database via the EntityManager API
        em.persist(customer);
        
        return customer;
    }

    private CustomerOrder addOrder(Customer customer, ShoppingCart cart) {
        
        //setup customer order
        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setAmount(BigDecimal.valueOf(cart.getTotal()));
        
        //create confirmation order number
        Random random = new Random();
        int i = random.nextInt(999999999);
        order.setConfirmationNumber(i);
        
        // to wright to the database via the EntityManager API
        em.persist(order);
        
        return order;
    }

    private void addOrderedItems(CustomerOrder order, ShoppingCart cart) {
        
//        Since the ID is an auto-incrementing primary key, the database 
//        automatically generates the value only when the record is added. 
//        One way to overcome this is to manually synchronize the persistence 
//        context with the database. 
//        This can be accomplished using the EntityManager's flush method.
        em.flush();
        
        List<ShoppingCartItem> items = cart.getItems();
        
        //iterate through ShoppingCartItem list and create orderedProducts
        for (ShoppingCartItem scItem : items) {
            
            int productId = scItem.getProduct().getId();
            
            // set up primary key object
            OrderedProductPK orderedProductPK = new OrderedProductPK();
            orderedProductPK.setCustomerOrderId(order.getId());
            orderedProductPK.setProductId(productId);
            
            // create ordered item using PK Object
            OrderedProduct orderedItem = new OrderedProduct(orderedProductPK);
            
            // set quantity
            orderedItem.setQuantity(scItem.getQuantity());
            
            // wright to the database via the EntityManager API
            em.persist(orderedItem);
        }
        
    }

}
