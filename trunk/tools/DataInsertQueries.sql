insert into next_object_no values('1', 'UserRegistrationTbl', '1', 'N', '1', NULL);
insert into next_object_no values('1', 'ConnectedUser', '1', 'N', '1', NULL);
INSERT INTO next_object_no(`non_id`, `non_object_name`, `non_current_no`, `delete_fl`, `version_id`, `ptn_id`) VALUES ('2', 'UserLoginInfo', '1', 'N', '1', '1');


CREATE TABLE `user_extra_detail` (
  `ued_Id` INT NOT NULL,
  `usr_Id` INT NULL,
  `adr_Id` INT NULL,
  `preferred_Cty_Id` INT NULL,
  `delete_Fl` CHAR(1) NULL,
  `version_id` INT NULL,
  `ptn_id` INT NULL,
  PRIMARY KEY (`ued_Id`));

--INSERT INTO `user_extra_detail` (`ued_Id`, `usr_Id`, `adr_Id`, `preferred_Cty_Id`, `delete_Fl`, `version_id`, `ptn_id`) VALUES ('1', '1', '1', '1', 'N', '1', '1');

insert into image_group values ('1', 'StoreLogos', 'N', '1', '1');

insert into image_tbl values ('1', 'MoreLogo', '/images/store-logos/more.jpg', 'N', '1', '1', '1');
insert into image_tbl values ('2', 'RelianceFresh', '/images/store-logos/RelianceFresh.png', 'N', '1', '1', '1');
insert into image_tbl values ('3', 'Central', '/images/store-logos/Central.jpg', 'N', '1', '1', '1');
insert into image_tbl values ('4', 'Hypercity', '/images/store-logos/Hypercity.png', 'N', '1', '1', '1');
insert into image_tbl values ('5', 'Lifestyle', '/images/store-logos/Lifestyle.jpg', 'N', '1', '1', '1');
insert into image_tbl values ('6', 'Max', '/images/store-logos/Max.png', 'N', '1', '1', '1');
insert into image_tbl values ('7', 'MomNMe', '/images/store-logos/MomNMe.png', 'N', '1', '1', '1');
insert into image_tbl values ('8', 'Pantaloons', '/images/store-logos/Pantalons.png', 'N', '1', '1', '1');

insert into address values ('1', '1', '88', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Marathahalli', '560037', 'N', '1', '1');
insert into address values ('2', '1', '97', 'Marathahalli - Sarjapur Outer Ring Rd', 'Mahadevpura', 'Mahadevpura', '560037', 'N', '1', '1');
insert into address values ('3', '1', '53', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Bellandur', '560037', 'N', '1', '1');
insert into address values ('4', '1', '33', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Kundalhalli Gate', '560037', 'N', '1', '1');
insert into address values ('5', '1', '33', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Domlur', '560037', 'N', '1', '1');
insert into address values ('6', '1', '33', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Marathalli', '560037', 'N', '1', '1');
insert into address values ('7', '1', '33', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Marathalli', '560037', 'N', '1', '1');
insert into address values ('8', '1', '33', 'Marathahalli - Sarjapur Outer Ring Rd', 'Bellandur', 'Brookfield', '560037', 'N', '1', '1');

insert into store_tbl values('1', 'More', 'MRE', NULL, 'N', '1', '1', '1');
insert into store_tbl values('2', 'Reliance Fresh', 'REF', NULL, 'N', '1', '1', '2');
insert into store_tbl values('3', 'Central', 'CTL', NULL, 'N', '1', '1', '3');
insert into store_tbl values('4', 'Hypercity', 'HYP', NULL, 'N', '1', '1', '4');
insert into store_tbl values('5', 'Lifestyle', 'LST', NULL, 'N', '1', '1', '5');
insert into store_tbl values('6', 'Max', 'MAX', NULL, 'N', '1', '1', '6');
insert into store_tbl values('7', 'Mom & Me', 'MNM', NULL, 'N', '1', '1', '7');
insert into store_tbl values('8', 'Pantaloons', 'PNT', NULL, 'N', '1', '1', '8');

insert into store_branch values ('1', '1', 'MRE', '800', '2230', NULL, 'N', '1', '1', '1', '1');
insert into store_branch values ('2', '2', 'REF', '1030', '2300', NULL, 'N', '1', '1', '1', '2');
insert into store_branch values ('3', '3', 'CTL', '1330', '2300', NULL, 'N', '1', '1', '1', '3');
insert into store_branch values ('4', '4', 'HYP', '1030', '2300', NULL, 'N', '1', '1', '1', '3');
insert into store_branch values ('5', '5', 'LST', '1000', '2230', NULL, 'N', '1', '1', '1', '3');
insert into store_branch values ('6', '6', 'MAX', '0930', '2200', NULL, 'N', '1', '1', '1', '3');
insert into store_branch values ('7', '7', 'MNM', '0900', '2130', NULL, 'N', '1', '1', '1', '3');
insert into store_branch values ('8', '8', 'PNT', '1000', '2230', NULL, 'N', '1', '1', '1', '3');

