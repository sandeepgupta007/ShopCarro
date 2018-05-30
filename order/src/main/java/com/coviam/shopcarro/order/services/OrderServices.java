package com.coviam.shopcarro.order.services;

import com.coviam.shopcarro.order.details.MerchantProductDetails;
import com.coviam.shopcarro.order.details.OrderDetails;
import com.coviam.shopcarro.order.dto.OrderDto;
import com.coviam.shopcarro.order.model.Order;
import com.coviam.shopcarro.order.repository.OrderRepository;
import com.coviam.shopcarro.order.utility.SendMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static com.coviam.shopcarro.order.utility.Urls.*;

/**
 *
 * @author: sandeepgupta
 * Yet to be done with expression language like urls and email login credentials.
 *
 * */

@Service
public class OrderServices implements IOrderservices {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
     private JavaMailSender javaMailSender;

    public SendMail sendMail;

    public List<OrderDetails> productBuy(OrderDto orderDto){
        /**
         *  List of orders that are present in the cart are in the OrderDto for that particular user.
         * */

        List<OrderDetails> listOfProductsPurchased = new ArrayList<>();

        for (OrderDetails details:orderDto.getDetails()){

            /**
             *
             * get the stocks for a particular a particular product and merchant Id.
             *
             *          @params: productId, merchantId and Quantity
             *
             *         Calling the merchant services to get the required availability
             *
             * */
            String urlGetAvailable = urlGetAvailableMerchant + details.getMerchantId()+"&productId="+details.getId()+"&quantity="+details.getQuantity();
            System.out.println(urlGetAvailable);
            RestTemplate restTemplate= new RestTemplate();
            Boolean availability;
            availability = restTemplate.getForObject(urlGetAvailable,Boolean.class);

            if(availability){

                /**
                 * Getting date
                 * */
                String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());

                OrderDetails orderDetails = new OrderDetails(details.getId(),details.getMerchantId(),details.getMerchantName(),details.getProductName(),details.getImageUrl(),timeStamp,details.getQuantity(),details.getPrice());

                listOfProductsPurchased.add(orderDetails);
                /**
                 *  link to decrement the stock.
                 * */
                String decrementStock = urlDecrementStock + details.getMerchantId()+"&productId="+details.getId()+"&quantity="+details.getQuantity();
                RestTemplate restTemplate3= new RestTemplate();
                Boolean decremented;
                decremented = restTemplate3.getForObject(decrementStock,Boolean.class);
            }
        }
        List <OrderDetails> listofProductsAlreadyPurchased = new ArrayList<>();
        //Order order = new Order();
        if(orderRepository.existsById(orderDto.getEmail())){
            listofProductsAlreadyPurchased = orderRepository.findById(orderDto.getEmail()).get().getDetails();
            listofProductsAlreadyPurchased.addAll(listOfProductsPurchased);
        }
        Order order = new Order(orderDto.getEmail(),listofProductsAlreadyPurchased);
        orderRepository.save(order);
        System.out.println(order.getEmail());
        System.out.println(listOfProductsPurchased);
        System.out.println("Sending Email");

       try {
            Sendingemail(order.getEmail(),listOfProductsPurchased,"Order Placed");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        String urlGetCart = "http://10.177.1.101:8081/del-all/?email=" + orderDto.getEmail();
        System.out.println("Deleting from cart " + urlGetCart);
        RestTemplate restTemplate= new RestTemplate();
        Boolean deletedFromCart = restTemplate.getForObject(urlGetCart,Boolean.class);

        return listOfProductsPurchased;
    }

    /**
     *
     *  Mail for sending the Simple Text messages using JavaMailSender
     *  This function was used previously when we were sending the text messages to the user with only product names
     *
     * */

    public void Sendemail(String email, List<OrderDetails> details,String subject){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("shopcarroecommerce@gmail.com");
        mail.setTo(email);
        mail.setSubject(subject);
       // String emailText = "Thanks for ordering from ShhoppCarro .Your product";
        String emailText = "Thanks for ordering from ShopCarro .Your ";
        if(details.size() == 1){
            emailText = emailText+"product is ";
        }
        else{
            emailText = emailText+"products are ";
        }
        emailText = emailText + "\n";

        Integer count = 0;
        for(OrderDetails orderDetails: details){
            emailText = emailText + (count+1) + " .";
            emailText = emailText + orderDetails.getProductName() + " by " + orderDetails.getMerchantId();
            String inlineImage = "<img src=\""+ orderDetails.getImageUrl()  + "\"></img><br/>";
            emailText += inlineImage;
            count+=1;
        }

        emailText = emailText + "\n Thanks for placing the Order";
        mail.setText(emailText);
        javaMailSender.send(mail);
    }

    /**
     *
     *  Will be returning the history if present otherwise this will be returning the empty list.
     *
     * */

    public List<OrderDetails> getHistoryOfUser(String email) throws ParseException{
        Optional<Order> order = Optional.of(new Order());
        order = orderRepository.findById(email);
        List<OrderDetails> list = new ArrayList<>();
        if(orderRepository.existsById(email)) {
            list = order.get().getDetails();
        }
        return list;
    }

    /**
     *  for single product purchase.
     *
     * */

    public Long purchaseProduct(String email, String productId, String merchantId, String quantity, String productName){
        String urlGetAvailable = urlGetAvailableMerchant + merchantId +"&productId="+ productId +"&quantity="+ quantity;
        System.out.println(urlGetAvailable);
        RestTemplate restTemplate= new RestTemplate();
        Boolean availability;
        availability = restTemplate.getForObject(urlGetAvailable,Boolean.class);

        if(availability) {

            /**
             * fetching the merchantName and price from merchant server
             * */

            String urlMerchantProductDetail = urlMerchantProductDetails + merchantId + "&productId=" + productId + "&quantity=" + quantity;
            System.out.println(" Getting merchant product Details: " + urlMerchantProductDetail);
            RestTemplate restTemplate1 = new RestTemplate();
            MerchantProductDetails merchantProductDetails;
            merchantProductDetails = restTemplate1.getForObject(urlMerchantProductDetail, MerchantProductDetails.class);

            /**
             *
             * fetching the image url from product server
             *
             * */
            String getImageUrl = urlProductImage + productId;
            System.out.println("Getting image url " + getImageUrl);
            RestTemplate restTemplate2 = new RestTemplate();
            String imageUrl = restTemplate2.getForObject(getImageUrl,String.class);

            /**
             * Getting date
             * */
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());

            OrderDetails orderDetails = new OrderDetails(productId,merchantId,merchantProductDetails.getMerchantName(),productName,imageUrl,timeStamp,quantity,merchantProductDetails.getPrice());

            String decrementStock = urlDecrementStock + merchantId +"&productId="+ productId +"&quantity="+ quantity;
            RestTemplate restTemplate3= new RestTemplate();
            Boolean decremented;
            decremented = restTemplate3.getForObject(decrementStock,Boolean.class);

            List<OrderDetails> listOfProductsPurchased = new ArrayList<>();
            listOfProductsPurchased.add(orderDetails);

            List <OrderDetails> listofProductsAlreadyPurchased = new ArrayList<>();
            //Order order = new Order();
            if(orderRepository.existsById(email)){
                listofProductsAlreadyPurchased = orderRepository.findById(email).get().getDetails();
                listofProductsAlreadyPurchased.addAll(listOfProductsPurchased);
            }
            Order order = new Order(email,listofProductsAlreadyPurchased);
            orderRepository.save(order);
            System.out.println(order.getEmail());
            System.out.println(listOfProductsPurchased);
            System.out.println("Sending Email");
            try {
                Sendingemail(order.getEmail(),listOfProductsPurchased,"Order Placed");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return Long.valueOf(1);
        }
        return Long.valueOf(0);
    }

    /**
     * Mail for sending the images with mail using javaMailSender using MimeMessage
     *
     * */

    public void Sendingemail(String email, List<OrderDetails> details,String subject) throws MessagingException {
        System.out.println("Sending mail ");
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        String emailText = "Thanks for ordering from ShopCarro. Your ";
        if(details.size() == 1){
            emailText = emailText+"product is ";
        }
        else{
            emailText = emailText+"products are ";
        }
        emailText = emailText + "\n";

        Integer count = 0;
        for(OrderDetails orderDetails: details){
            emailText = emailText + (count+1) + ". ";
            emailText = emailText + orderDetails.getProductName() + " by " + orderDetails.getMerchantId() + "\n";
            String inlineImage = "<br></br><img src=\""+ orderDetails.getImageUrl()  + "\" width=\"100\" height=\"70\" ></img><br/>";
            emailText += inlineImage;
            count+=1;
        }

        helper.setText(emailText, true);
        helper.setSubject(subject);
        helper.setTo(email);
        helper.setFrom("shopcarroecommerce@gmail.com");
        javaMailSender.send(message);
        return;
    }
}