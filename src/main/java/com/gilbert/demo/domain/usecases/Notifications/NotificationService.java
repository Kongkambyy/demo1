package com.gilbert.demo.domain.usecases.Notifications;

import com.gilbert.demo.data.repository.NotificationRepository;
import com.gilbert.demo.data.repository.FavoriteRepository;
import com.gilbert.demo.domain.entities.Notification;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FavoriteRepository favoriteRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               FavoriteRepository favoriteRepository) {
        this.notificationRepository = notificationRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public void notifyPriceChange(String listingId, String sellerId, String listingTitle, int oldPrice, int newPrice) {
        List<String> userIds = getUsersWhoFavoritedListing(listingId);

        String message = "The price of \"" + listingTitle + "\" has changed from "
                + oldPrice + " DKK to " + newPrice + " DKK.";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        for (String userId : userIds) {
            Notification notification = new Notification(
                    userId,
                    listingId,
                    "LISTING",
                    "FAVORITE_PRICE_CHANGE",
                    message,
                    createdDate
            );

            notificationRepository.save(notification);
            LoggerUtility.logEvent("Price change notification created for user: " + userId);
        }
    }

    public void notifyOfferOnFavoritedListing(String listingId, String offerId, String listingTitle, int offerAmount) {

        List<String> userIds = getUsersWhoFavoritedListing(listingId);

        String message = "An offer of " + offerAmount + " DKK has been made on \"" + listingTitle + "\" that you favorited.";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (String userId : userIds) {
            Notification notification = new Notification(
                    userId,
                    listingId,
                    "LISTING",
                    "FAVORITE_OFFER",
                    message,
                    createdDate
            );

            notificationRepository.save(notification);
            LoggerUtility.logEvent("Offer notification created for user: " + userId);
        }
    }

    public void notifyFavoritedListingSold(String listingId, String listingTitle) {

        List<String> userIds = getUsersWhoFavoritedListing(listingId);

        String message = "The listing \"" + listingTitle + "\" that you favorited has been sold.";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (String userId : userIds) {
            Notification notification = new Notification(
                    userId,
                    listingId,
                    "LISTING",
                    "FAVORITE_SOLD",
                    message,
                    createdDate
            );

            notificationRepository.save(notification);
            LoggerUtility.logEvent("Listing sold notification created for user: " + userId);
        }
    }

    public void notifyListingSold(String sellerId, String listingId, String listingTitle, int price) {
        String message = "Congratulations! Your listing \"" + listingTitle + "\" has been sold for " + price + " DKK.";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Notification notification = new Notification(
                sellerId,
                listingId,
                "LISTING",
                "ITEM_SOLD",
                message,
                createdDate
        );

        notificationRepository.save(notification);
        LoggerUtility.logEvent("Sale notification created for seller: " + sellerId);
    }

    public void notifyOfferReceived(String sellerId, String listingId, String offerId, String listingTitle, int offerAmount) {
        String message = "You have received an offer of " + offerAmount + " DKK for your listing \"" + listingTitle + "\".";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Notification notification = new Notification(
                sellerId,
                offerId,
                "OFFER",
                "OFFER_RECEIVED",
                message,
                createdDate
        );

        notificationRepository.save(notification);
        LoggerUtility.logEvent("Offer received notification created for seller: " + sellerId);
    }

    public void notifyOfferSubmitted(String buyerId, String offerId, String listingId, String listingTitle, int offerAmount) {
        String message = "Your offer of " + offerAmount + " DKK for \"" + listingTitle + "\" has been submitted successfully. You will be notified when the seller responds.";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Notification notification = new Notification(
                buyerId,
                offerId,
                "OFFER",
                "OFFER_SUBMITTED",
                message,
                createdDate
        );

        notificationRepository.save(notification);
        LoggerUtility.logEvent("Offer submitted notification created for buyer: " + buyerId);
    }

    public void notifyOfferAccepted(String buyerId, String offerId, String listingId, String listingTitle, int offerAmount) {
        String message = "Your offer of " + offerAmount + " DKK for \"" + listingTitle + "\" has been accepted!";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Notification notification = new Notification(
                buyerId,
                offerId,
                "OFFER",
                "OFFER_ACCEPTED",
                message,
                createdDate
        );

        notificationRepository.save(notification);
        LoggerUtility.logEvent("Offer accepted notification created for buyer: " + buyerId);
    }

    public void notifyOfferRejected(String buyerId, String offerId, String listingId, String listingTitle, int offerAmount) {
        String message = "Your offer of " + offerAmount + " DKK for \"" + listingTitle + "\" has been rejected.";

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Notification notification = new Notification(
                buyerId,
                offerId,
                "OFFER",
                "OFFER_REJECTED",
                message,
                createdDate
        );

        notificationRepository.save(notification);
        LoggerUtility.logEvent("Offer rejected notification created for buyer: " + buyerId);
    }

    private List<String> getUsersWhoFavoritedListing(String listingId) {
        return favoriteRepository.findUsersByListingId(listingId);
    }
}