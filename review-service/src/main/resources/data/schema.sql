create table reviews (
    id int NOT NULL AUTO_INCREMENT,
    product_id int,
    review_id int,
    author   char(255),
    subject  char(255),
    content  varchar(3000),
    version int,
    PRIMARY KEY (id),
    CONSTRAINT reviews_unique_idx UNIQUE (product_id, review_id)
);