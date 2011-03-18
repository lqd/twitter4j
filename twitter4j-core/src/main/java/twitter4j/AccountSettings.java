/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.9
 */
public interface AccountSettings extends TwitterResponse, java.io.Serializable {
    /**
     * Returns true if the user enabled sleep time.
     *
     * @return true if the user enabled sleep time
     */
    boolean isSleepTimeEnabled();

    /**
     * Returns sleep start time.
     *
     * @return sleep start time
     */
    String getSleepStartTime();

    /**
     * Returns sleep end time.
     *
     * @return sleep end time
     */
    String getSleepEndTime();

    /**
     * Return the user's trend locations
     *
     * @return the user's trend locations
     */
    Location[] getTrendLocations();

    /**
     * Return true if the user is enabling geo location
     *
     * @return true if the user is enabling geo location
     */
    boolean isGeoEnabled();
}
