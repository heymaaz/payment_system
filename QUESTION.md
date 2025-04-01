# Payment Processing System – Interview Task

## At-home Assignment (4-8 hours)

### Task Description:

You are building a simplified payment processing system. The system should:
- Allow users to register an account with an initial balance.
- Allow users to make payments to other users. Each payment must include:
  - Sender account
  - Receiver account 
  - Amount 
  - Timestamp 
  - Unique Payment ID
- Prevent users from sending more money than they have. 
- Maintain a transaction history. 
- Expose a API for:
  - Registering a new user 
  - Fetching user details (including balance and transaction history)
  - Making a payment 
  - Listing all payments (newest first)
- Ensure basic logging and error handling for:
  - Invalid users 
  - Insufficient balance 
  - Negative payment amounts

### Expectations:

- "Production-ready" code: Runnable, clean, modular, and maintainable in Java or any other modern programming language. 
- Simple Storage: In-memory storage is sufficient, as the database is not anticipated. 
- Well-structured API: Use proper request/response models. 
- Efficient algorithms and data structures. 
- Tests that provide confidence that your solution works. 
- Feel free to use the IDE of your choice. 
- Code should be your own work - please do not submit anything AI generated!
