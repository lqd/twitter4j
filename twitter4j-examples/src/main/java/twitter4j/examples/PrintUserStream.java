/*
Copyright (c) 2007-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package twitter4j.examples;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.conf.PropertyConfiguration;

/**
 * <p>
 * This is a code example of Twitter4J Streaming API - user stream.<br>
 * Usage: java twitter4j.examples.PrintUserStream. Needs a valid
 * twitter4j.properties file with OAuth properties set<br>
 * </p>
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @author Rémy Rakic - remy dot rakic at gmail.com
 */
public final class PrintUserStream implements UserStreamListener
{
    public static void main (String [] args) throws TwitterException
    {
        PrintUserStream printUserStream = new PrintUserStream (args);
        printUserStream.startConsuming ();
    }

    private TwitterStream twitterStream;

    // used for getting user info
    private Twitter twitter;
    private long currentUserId;

    // useful to rapidly filter out replies from people you follow to people you don't follow
    private static boolean ALL_REPLIES_FROM_FOLLOWINGS = true;
    
    // useful to rapidly filter out replies from people you don't follow to people you follow.
    private static boolean ALL_REPLIES_TO_FOLLOWINGS = false;
    
    private static boolean SHOW_TWEETS_URL_ON_TWITTER_WEBSITE = false;
    
    public PrintUserStream (String [] args)
    {
        Configuration properties = new PropertyConfiguration (getClass ().getResourceAsStream ("twitter4j.properties"));
        
        boolean enableAllReplies = ALL_REPLIES_FROM_FOLLOWINGS || ALL_REPLIES_TO_FOLLOWINGS;
        Configuration conf = new ConfigurationBuilder ().setOAuthConsumerKey (properties.getOAuthConsumerKey ())
                                                        .setOAuthConsumerSecret (properties.getOAuthConsumerSecret ())
                                                        .setOAuthAccessToken (properties.getOAuthAccessToken ())
                                                        .setOAuthAccessTokenSecret (properties.getOAuthAccessTokenSecret ())
                                                        .setUserStreamRepliesAllEnabled (enableAllReplies)
                                                        .setIncludeRTsEnabled (true)
                                                        .build ();
        
        twitterStream = new TwitterStreamFactory (conf).getInstance ();
        twitter = new TwitterFactory (conf).getInstance ();

        try
        {
            User currentUser = twitter.verifyCredentials ();
            currentUserId = currentUser.getId ();
        }
        catch (TwitterException e)
        {
            System.out.println ("Unexpected exception caught while trying to retrieve the current user: " + e);
            e.printStackTrace ();
        }

        Timer t = new Timer (5 * 60 * 1000, new ActionListener ()
        {
            public void actionPerformed (ActionEvent e)
            {
                System.out.println ("");
            }
        });

        t.start ();
    }

    private void startConsuming ()
    {
        // the user() method internally creates a thread which manipulates
        // TwitterStream and calls these adequate listener methods continuously.
        twitterStream.addListener (this);
        twitterStream.user ();
    }

    private Set<Long> friends;

    @Override
    public void onFriendList (long [] friendIds)
    {
        System.out.println ("Received friends list - Following " + friendIds.length + " people");

        friends = new HashSet<Long> (friendIds.length);
        for (long id : friendIds)
            friends.add (id);
    }

    @Override
    public void onStatus (Status status)
    {
        long replyTo = status.getInReplyToUserId ();
        
        User user = status.getUser ();
        
        if (replyTo > 0)
        {
            long from = user.getId ();
            String replyType = null;
            
            if (currentUserId != replyTo)
            {
                // reply *to* someone you follow, or *from* someone you follow
                if (friends.contains (from))
                {
                    // reply from someone you follow
                    if (! friends.contains (replyTo))
                    {
                        // to someone you don't follow
                        if (! ALL_REPLIES_FROM_FOLLOWINGS)
                            return;
                       replyType = "[To stranger]";
                    }
                }
                else if (currentUserId != from) // the reply is not from you
                {
                    // reply from someone you don't follow to someone you follow
                    if (! ALL_REPLIES_TO_FOLLOWINGS)
                        return;
                    replyType = "[From stranger]";
                }
            }
            else
            {
                // reply to you
                if (! friends.contains (from))
                    replyType = "[From stranger]";
            }
            
            if (replyType != null)
                System.out.print (replyType + " ");
        }
        
        System.out.print (user.getName () + " [" + user.getScreenName () + "] : " + status.getText ());
        
        if (SHOW_TWEETS_URL_ON_TWITTER_WEBSITE)
            System.out.println (" - link: http://twitter.com/" + user.getScreenName () + "/status/" + status.getId ());
        else
            System.out.println ("");
    }

    @Override
    public void onDirectMessage (DirectMessage dm)
    {
        System.out.println ("DM from " + dm.getSenderScreenName () + " to " + dm.getRecipientScreenName () + ": " + dm.getText ());
    }

    @Override
    public void onDeletionNotice (StatusDeletionNotice notice)
    {
        User user = friend (notice.getUserId ());
        if (user == null)
            return;
        System.out.println (user.getName () + " [" + user.getScreenName () + "] deleted the tweet " + notice.getStatusId ());
    }

    private User friend (long userId)
    {
        try
        {
            return twitter.showUser (userId);
        }
        catch (TwitterException e)
        {
            System.out.println ("Unexpected exception caught while trying to show user " + userId + ": " + e);
            e.printStackTrace ();
        }

        return null;
    }

    @Override
    public void onTrackLimitationNotice (int numberOfLimitedStatuses)
    {
        System.out.println ("track limitation: " + numberOfLimitedStatuses);
    }

    @Override
    public void onException (Exception ex)
    {
        ex.printStackTrace ();
    }

    @Override
    public void onFavorite (User source, User target, Status favoritedStatus)
    {
        System.out.println (source.getName () + " [" + source.getScreenName () + "] favorited "
                + target.getName () + "'s [" + target.getScreenName () + "] tweet: "
                + favoritedStatus.getText ());
    }

    @Override
    public void onUnfavorite (User source, User target, Status unfavoritedStatus)
    {
        System.out.println (source.getName () + " [" + source.getScreenName () + "] unfavorited "
                + target.getName () + "'s [" + target.getScreenName () + "] tweet: "
                + unfavoritedStatus.getText ());
    }

    @Override
    public void onFollow (User source, User target)
    {
        System.out.println (source.getName () + " [" + source.getScreenName () + "] started following "
                + target.getName () + " [" + target.getScreenName () + "]");
    }

    public void onUnfollow (User source, User target)
    {
        System.out.println (source.getName () + " [" + source.getScreenName () + "] unfollowed "
                + target.getName () + " [" + target.getScreenName () + "]");

        if (source.getId () == currentUserId)
            friends.remove (target);
    }

    @Override
    public void onRetweet (User source, User target, Status retweetedStatus)
    {
    }

    @Override
    public void onBlock (User source, User target)
    {
        System.out.println (source.getName () + " [" + source.getScreenName () + "] blocked "
                + target.getName () + " [" + target.getScreenName () + "]");
    }

    @Override
    public void onUnblock (User source, User target)
    {
        System.out.println (source.getName () + " [" + source.getScreenName () + "] unblocked "
                + target.getName () + " [" + target.getScreenName () + "]");
    }

    @Override
    public void onUserListCreation (User listOwner, UserList list)
    {
        System.out.println (listOwner.getName () + " [" + listOwner.getScreenName () + "] created list: "
                + list.getName () + " [" + list.getFullName () + "]");
    }

    @Override
    public void onUserListDeletion (User listOwner, UserList list)
    {
        System.out.println (listOwner.getName () + " [" + listOwner.getScreenName () + "] destroyed list: "
                + list.getName () + " [" + list.getFullName () + "]");
    }

    @Override
    public void onUserListSubscription (User subscriber, User listOwner, UserList list)
    {
        System.out.println (subscriber.getName () + " [" + subscriber.getScreenName () + "] subscribed to "
                + listOwner.getName () + "'s [" + listOwner.getScreenName () + "] list: " + list.getName ()
                + " [" + list.getFullName () + "]");
    }
    
    @Override
    public void onUserListUpdate (User listOwner, UserList list)
    {
        System.out.println (listOwner.getName () + " [" + listOwner.getScreenName () + "] updated list: "
                + list.getName () + " [" + list.getFullName () + "]");
    }
    
    @Override
    public void onScrubGeo (long userId, long upToStatusId)
    {
        System.out.println ("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }
    
    @Override
    public void onDeletionNotice (long directMessageId, long userId)
    {
        System.out.println ("Got a direct message deletion notice id:" + directMessageId);
    }
    
    @Override
    public void onUserProfileUpdate (User updatedUser)
    {
        System.out.println ("onUserProfileUpdated user:@" + updatedUser.getScreenName());
    }

    @Override
    public void onUserListMemberAddition (User addedMember, User listOwner, UserList list)
    {
    }

    @Override
    public void onUserListMemberDeletion (User deletedMember, User listOwner, UserList list)
    {
    }

    @Override
    public void onUserListUnsubscription (User subscriber, User listOwner, UserList list)
    {
    }
}