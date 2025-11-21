/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an address in aviation messaging (NAL format).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

  /**
   * Address type (e.g., NAL)
   */
  private String type;

  /**
   * City code (3 letters)
   */
  private String cityCode;

  /**
   * Department code (2 letters)
   */
  private String departmentCode;

  /**
   * Company code (2 letters/numbers)
   */
  private String companyCode;

  /**
   * Full address string
   */
  private String address;
}

